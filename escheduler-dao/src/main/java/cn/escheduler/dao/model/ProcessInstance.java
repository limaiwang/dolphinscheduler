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
package cn.escheduler.dao.model;

import cn.escheduler.common.enums.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * process instance
 */
public class ProcessInstance {

    /**
     * id
     */
    private int id;
    /**
     * process definition id
     */
    private int processDefinitionId;
    /**
     * process state
     */
    private ExecutionStatus state;
    /**
     * recovery flag for failover
     */
    private Flag recovery;
    /**
     * start time
     */
    private Date startTime;

    /**
     * end time
     */
    private Date endTime;

    /**
     * run time
     */
    private int runTimes;

    /**
     * name
     */
    private String name;

    /**
     * host
     */
    private String host;

    /**
     * process definition structure
     */
    private ProcessDefinition processDefinition;
    /**
     * process command type
     */
    private CommandType commandType;

    /**
     * command parameters
     */
    private String commandParam;

    /**
     * node depend type
     */
    private TaskDependType taskDependType;

    /**
     * task max try times
     */
    private int maxTryTimes;

    /**
     * failure strategy when task failed.
     */
    private FailureStrategy failureStrategy;

    /**
     *  warning type
     */
    private WarningType warningType;

    /**
     * warning group
     */
    private Integer warningGroupId;

    /**
     * schedule time
     */
    private Date scheduleTime;

    /**
     * command start time
     */
    private Date commandStartTime;

    /**
     * user define parameters string
     */
    private String globalParams;

    /**
     * process instance json
     */
    private String processInstanceJson;

    /**
     * executor id
     */
    private int executorId;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * queue
     */
    private String queue;

    /**
     * process is sub process
     */
    private Flag isSubProcess;

    /**
     * task locations for web
     */
    private String locations;

    /**
     * task connects for web
     */
    private String connects;

    /**
     * history command
     */
    private String historyCmd;

    /**
     * depend processes schedule time
     */
    private String dependenceScheduleTimes;

    /**
     * process duration
     * @return
     */
    private Long duration;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;


    /**
     * worker group id
     */
    private int workerGroupId;

    /**
     * process timeout for warning
     */
    private int timeout;


    /**
     * tenant id
     */
    private int tenantId;

    public ProcessInstance(){

    }

    /**
     * set the process name with process define version and timestamp
     * @param processDefinition
     */
    public ProcessInstance(ProcessDefinition processDefinition){
        this.processDefinition = processDefinition;
        this.name = processDefinition.getName() + "-" +
                processDefinition.getVersion() + "-" +
                System.currentTimeMillis();
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(int processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public ExecutionStatus getState() {
        return state;
    }

    public void setState(ExecutionStatus state) {
        this.state = state;
    }

    public Flag getRecovery() {
        return recovery;
    }

    public void setRecovery(Flag recovery) {
        this.recovery = recovery;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(int runTimes) {
        this.runTimes = runTimes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public String getCommandParam() {
        return commandParam;
    }

    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }

    public TaskDependType getTaskDependType() {
        return taskDependType;
    }

    public void setTaskDependType(TaskDependType taskDependType) {
        this.taskDependType = taskDependType;
    }


    public int getMaxTryTimes() {
        return maxTryTimes;
    }

    public void setMaxTryTimes(int maxTryTimes) {
        this.maxTryTimes = maxTryTimes;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }


    public boolean IsProcessInstanceStop(){
        return this.state.typeIsFinished();
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public Integer getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(Integer warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getCommandStartTime() {
        return commandStartTime;
    }

    public void setCommandStartTime(Date commandStartTime) {
        this.commandStartTime = commandStartTime;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public String getProcessInstanceJson() {
        return processInstanceJson;
    }

    public void setProcessInstanceJson(String processInstanceJson) {
        this.processInstanceJson = processInstanceJson;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }


    public Flag getIsSubProcess() {
        return isSubProcess;
    }

    public void setIsSubProcess(Flag isSubProcess) {
        this.isSubProcess = isSubProcess;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }
    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getConnects() {
        return connects;
    }

    public void setConnects(String connects) {
        this.connects = connects;
    }

    public String getHistoryCmd() {
        return historyCmd;
    }

    public void setHistoryCmd(String historyCmd) {
        this.historyCmd = historyCmd;
    }

    /**
     * add command to history
     * @param cmd
     */
    public void addHistoryCmd(CommandType cmd){
        if(StringUtils.isNotEmpty(this.historyCmd)){
            this.historyCmd = String.format("%s,%s", this.historyCmd, cmd.toString());
        }else{
            this.historyCmd = cmd.toString();
        }
    }

    /**
     * check this process is start complement data
     */
    public Boolean isComplementData(){
        if(!StringUtils.isNotEmpty(this.historyCmd)){
            return false;
        }
        return historyCmd.startsWith(CommandType.COMPLEMENT_DATA.toString());
    }

    /**
     * get current command type,
     * if start with complement data,return complement
     */
    public CommandType getCmdTypeIfComplement(){
        if(isComplementData()){
            return CommandType.COMPLEMENT_DATA;
        }
        return commandType;
    }

    public String getDependenceScheduleTimes() {
        return dependenceScheduleTimes;
    }

    public void setDependenceScheduleTimes(String dependenceScheduleTimes) {
        this.dependenceScheduleTimes = dependenceScheduleTimes;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getWorkerGroupId() {
        return workerGroupId;
    }

    public void setWorkerGroupId(int workerGroupId) {
        this.workerGroupId = workerGroupId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id=" + id +
                ", processDefinitionId=" + processDefinitionId +
                ", state=" + state +
                ", recovery=" + recovery +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", runTimes=" + runTimes +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", processDefinition=" + processDefinition +
                ", commandType=" + commandType +
                ", commandParam='" + commandParam + '\'' +
                ", taskDependType=" + taskDependType +
                ", maxTryTimes=" + maxTryTimes +
                ", failureStrategy=" + failureStrategy +
                ", warningType=" + warningType +
                ", warningGroupId=" + warningGroupId +
                ", scheduleTime=" + scheduleTime +
                ", commandStartTime=" + commandStartTime +
                ", globalParams='" + globalParams + '\'' +
                ", processInstanceJson='" + processInstanceJson + '\'' +
                ", executorId=" + executorId +
                ", tenantCode='" + tenantCode + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", queue='" + queue + '\'' +
                ", isSubProcess=" + isSubProcess +
                ", locations='" + locations + '\'' +
                ", connects='" + connects + '\'' +
                ", historyCmd='" + historyCmd + '\'' +
                ", dependenceScheduleTimes='" + dependenceScheduleTimes + '\'' +
                ", duration=" + duration +
                ", timeout=" + timeout +
                ", processInstancePriority=" + processInstancePriority +
                '}';
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return this.tenantId ;
    }
}
