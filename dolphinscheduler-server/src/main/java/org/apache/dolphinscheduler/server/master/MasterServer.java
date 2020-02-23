/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor;
import org.apache.dolphinscheduler.server.master.runner.MasterSchedulerThread;
import org.apache.dolphinscheduler.server.zk.ZKMasterClient;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.ProcessScheduleJob;
import org.apache.dolphinscheduler.service.quartz.QuartzExecutors;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * master server
 */
@ComponentScan("org.apache.dolphinscheduler")
public class MasterServer implements IStoppable {

    /**
     * logger of MasterServer
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterServer.class);

    /**
     *  zk master client
     */
    @Autowired
    private ZKMasterClient zkMasterClient = null;

    /**
     *  heartbeat thread pool
     */
    private ScheduledExecutorService heartbeatMasterService;

    /**
     *  process service
     */
    @Autowired
    protected ProcessService processService;

    /**
     *  master exec thread pool
     */
    private ExecutorService masterSchedulerService;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;


    /**
     *  spring application context
     *  only use it for initialization
     */
    @Autowired
    private SpringApplicationContext springApplicationContext;

    private NettyRemotingServer nettyRemotingServer;


    /**
     * master server startup
     *
     * master server not use web service
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_MASTER_SERVER);
        new SpringApplicationBuilder(MasterServer.class).web(WebApplicationType.NONE).run(args);
    }

    /**
     * run master server
     */
    @PostConstruct
    public void run(){

        //
        //init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(45678);
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.EXECUTE_TASK_RESPONSE, new TaskResponseProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.EXECUTE_TASK_ACK, new TaskAckProcessor());
        this.nettyRemotingServer.start();

        //
        zkMasterClient.init();

        masterSchedulerService = ThreadUtils.newDaemonSingleThreadExecutor("Master-Scheduler-Thread");

        heartbeatMasterService = ThreadUtils.newDaemonThreadScheduledExecutor("Master-Main-Thread",Constants.DEFAULT_MASTER_HEARTBEAT_THREAD_NUM);

        // heartbeat thread implement
        Runnable heartBeatThread = heartBeatThread();

        zkMasterClient.setStoppable(this);

        // regular heartbeat
        // delay 5 seconds, send heartbeat every 30 seconds
        heartbeatMasterService.
                scheduleAtFixedRate(heartBeatThread, 5, masterConfig.getMasterHeartbeatInterval(), TimeUnit.SECONDS);

        // master scheduler thread
        MasterSchedulerThread masterSchedulerThread = new MasterSchedulerThread(
                zkMasterClient,
                processService,
                masterConfig.getMasterExecThreads());

        // submit master scheduler thread
        masterSchedulerService.execute(masterSchedulerThread);

        // start QuartzExecutors
        // what system should do if exception
        try {
            logger.info("start Quartz server...");
            ProcessScheduleJob.init(processService);
            QuartzExecutors.getInstance().start();
        } catch (Exception e) {
            try {
                QuartzExecutors.getInstance().shutdown();
            } catch (SchedulerException e1) {
                logger.error("QuartzExecutors shutdown failed : " + e1.getMessage(), e1);
            }
            logger.error("start Quartz failed", e);
        }


        /**
         *  register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (zkMasterClient.getActiveMasterNum() <= 1) {
                    zkMasterClient.getAlertDao().sendServerStopedAlert(
                        1, OSUtils.getHost(), "Master-Server");
                }
                stop("shutdownhook");
            }
        }));
    }


    /**
     * gracefully stop
     * @param cause why stopping
     */
    @Override
    public synchronized void stop(String cause) {

        try {
            //execute only once
            if(Stopper.isStopped()){
                return;
            }

            logger.info("master server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                //thread sleep 3 seconds for thread quitely stop
                Thread.sleep(3000L);
            }catch (Exception e){
                logger.warn("thread sleep exception ", e);
            }
            try {
                heartbeatMasterService.shutdownNow();
            }catch (Exception e){
                logger.warn("heartbeat service stopped exception");
            }

            logger.info("heartbeat service stopped");

            //close quartz
            try{
                QuartzExecutors.getInstance().shutdown();
            }catch (Exception e){
                logger.warn("Quartz service stopped exception:{}",e.getMessage());
            }

            logger.info("Quartz service stopped");

            try {
                ThreadPoolExecutors.getInstance().shutdown();
            }catch (Exception e){
                logger.warn("threadpool service stopped exception:{}",e.getMessage());
            }

            logger.info("threadpool service stopped");

            try {
                masterSchedulerService.shutdownNow();
            }catch (Exception e){
                logger.warn("master scheduler service stopped exception:{}",e.getMessage());
            }

            logger.info("master scheduler service stopped");

            try {
                zkMasterClient.close();
            }catch (Exception e){
                logger.warn("zookeeper service stopped exception:{}",e.getMessage());
            }

            logger.info("zookeeper service stopped");


        } catch (Exception e) {
            logger.error("master server stop exception ", e);
            System.exit(-1);
        }
    }


    /**
     *  heartbeat thread implement
     * @return
     */
    private Runnable heartBeatThread(){
        logger.info("start master heart beat thread...");
        Runnable heartBeatThread  = new Runnable() {
            @Override
            public void run() {
                if(Stopper.isRunning()) {
                    // send heartbeat to zk
                    if (StringUtils.isBlank(zkMasterClient.getMasterZNode())) {
                        logger.error("master send heartbeat to zk failed: can't find zookeeper path of master server");
                        return;
                    }

                    zkMasterClient.heartBeatForZk(zkMasterClient.getMasterZNode(), Constants.MASTER_PREFIX);
                }
            }
        };
        return heartBeatThread;
    }
}

