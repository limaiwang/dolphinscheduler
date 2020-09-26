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

package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * alert of start
 */
public class AlertServer {
    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);
    /**
     * Alert Dao
     */
    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    private PluginDao pluginDao = DaoFactory.getDaoInstance(PluginDao.class);

    private AlertSender alertSender;

    private static AlertServer instance;

    private AlertPluginManager alertPluginManager;

    private DolphinPluginManagerConfig alertPluginManagerConfig;

    public static final String ALERT_PLUGIN_BINDING = "alert.plugin.binding";

    public static final String ALERT_PLUGIN_DIR = "alert.plugin.dir";

    public static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private static class AlertServerHolder {
        private static final AlertServer INSTANCE = new AlertServer();
    }

    public static final AlertServer getInstance() {
        return AlertServerHolder.INSTANCE;

    }

    private AlertServer() {

    }

    private void initPlugin() {
        alertPluginManager = new AlertPluginManager();
        alertPluginManagerConfig = new DolphinPluginManagerConfig();
        alertPluginManagerConfig.setPlugins(PropertyUtils.getString(ALERT_PLUGIN_BINDING));
        if (StringUtils.isNotBlank(PropertyUtils.getString(ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(ALERT_PLUGIN_DIR, Constants.ALERT_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }
    }

    public void start() {

        initPlugin();

        logger.info("alert server ready start ");
        while (Stopper.isRunning()) {
            try {
                Thread.sleep(Constants.ALERT_SCAN_INTERVAL);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            if (alertPluginManager == null || alertPluginManager.getAlertChannelMap().size() == 0) {
                logger.warn("No Alert Plugin . Can not send alert info. ");
            } else {
                List<Alert> alerts = alertDao.listWaitExecutionAlert();
                alertSender = new AlertSender(alerts, alertDao, alertPluginManager, pluginDao);
                alertSender.run();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        AlertServer alertServer = AlertServer.getInstance();
        alertServer.start();
    }

}
