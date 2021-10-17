/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.schedule.plugin;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyReceiverVo;
import codedriver.framework.notify.dto.ParamMappingVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.util.NotifyPolicyUtil;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import codedriver.module.process.notify.constvalue.SlaNotifyTriggerType;
import codedriver.framework.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@DisallowConcurrentExecution
public class ProcessTaskSlaNotifyJob extends JobBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskSlaNotifyJob.class);

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public Boolean isHealthy(JobObject jobObject) {
        Long slaTransferId = (Long) jobObject.getData("slaNotifyId");
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskSlaNotifyById(slaTransferId);
        if (processTaskSlaNotifyVo == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        String tenantUuid = jobObject.getTenantUuid();
        TenantContext.get().switchTenant(tenantUuid);
        Long slaNotifyId = (Long)jobObject.getData("slaNotifyId");
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskSlaNotifyById(slaNotifyId);
        boolean isJobLoaded = false;
        try{
            if (processTaskSlaNotifyVo != null) {
                ProcessTaskSlaTimeVo slaTimeVo =
                        processTaskMapper.getProcessTaskSlaTimeBySlaId(processTaskSlaNotifyVo.getSlaId());
                if (slaTimeVo != null) {
                    if (processTaskSlaNotifyVo != null && processTaskSlaNotifyVo.getConfigObj() != null) {
                        JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
                        String expression = policyObj.getString("expression");
                        int time = policyObj.getIntValue("time");
                        String unit = policyObj.getString("unit");
                        String executeType = policyObj.getString("executeType");
                        int intervalTime = policyObj.getIntValue("intervalTime");
                        String intervalUnit = policyObj.getString("intervalUnit");
                        Integer repeatCount = null;
                        if ("loop".equals(executeType) && intervalTime > 0) {// 周期执行
                            if (intervalUnit.equalsIgnoreCase("day")) {
                                intervalTime = intervalTime * 24 * 60 * 60;
                            } else {
                                intervalTime = intervalTime * 60 * 60;
                            }
                        } else {// 单次执行
                            repeatCount = 0;
                            intervalTime = 60 * 60;
                        }
                        Calendar notifyDate = Calendar.getInstance();
                        notifyDate.setTime(slaTimeVo.getExpireTime());
                        if (expression.equalsIgnoreCase("before")) {
                            time = -time;
                        }
                        if (StringUtils.isNotBlank(unit) && time != 0) {
                            if (unit.equalsIgnoreCase("day")) {
                                notifyDate.add(Calendar.DAY_OF_MONTH, time);
                            } else if (unit.equalsIgnoreCase("hour")) {
                                notifyDate.add(Calendar.HOUR, time);
                            } else {
                                notifyDate.add(Calendar.MINUTE, time);
                            }
                        }
                        /** 如果触发时间在当前时间之前 **/
                        if (notifyDate.before(Calendar.getInstance())) {
                            if("loop".equals(executeType)){
                                /** 如果是循环触发，则将触发时间改为当前时间 **/
                                notifyDate = Calendar.getInstance();
                            }else{
                                /** 如果是单次触发，不启动作业 **/
                                return;
                            }
                        }
                        JobObject.Builder newJobObjectBuilder =
                                new JobObject.Builder(processTaskSlaNotifyVo.getId().toString(), this.getGroupName(),
                                        this.getClassName(), TenantContext.get().getTenantUuid())
                                        .withBeginTime(notifyDate.getTime()).withIntervalInSeconds(intervalTime)
                                        .withRepeatCount(repeatCount).addData("slaNotifyId", processTaskSlaNotifyVo.getId());
                        JobObject newJobObject = newJobObjectBuilder.build();
                        Date triggerDate = schedulerManager.loadJob(newJobObject);
                        if (triggerDate != null) {
                            // 更新通知记录时间
                            processTaskSlaNotifyVo.setTriggerTime(triggerDate);
                            processTaskMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
                            isJobLoaded = true;
                        }
                    }
                }
            }
        }finally{
            if (!isJobLoaded) {
                // 没有加载到作业，则删除通知记录
                processTaskMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            }
        }
    }

    @Override
    public void initJob(String tenantUuid) {
        List<ProcessTaskSlaNotifyVo> slaNotifyList = processTaskMapper.getAllProcessTaskSlaNotify();
        for (ProcessTaskSlaNotifyVo processTaskSlaNotifyVo : slaNotifyList) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(processTaskSlaNotifyVo.getSlaId().toString(),
                this.getGroupName(), this.getClassName(), TenantContext.get().getTenantUuid()).addData("slaNotifyId",
                    processTaskSlaNotifyVo.getId());
            JobObject jobObject = jobObjectBuilder.build();
            this.reloadJob(jobObject);
        }
    }

    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {
        Long slaNotifyId = (Long)jobObject.getData("slaNotifyId");
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskSlaNotifyById(slaNotifyId);
        if (processTaskSlaNotifyVo != null) {
            Long slaId = processTaskSlaNotifyVo.getSlaId();
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
            Iterator<ProcessTaskStepVo> it = processTaskStepList.iterator();
            while (it.hasNext()) {
                ProcessTaskStepVo processTaskStepVo = it.next();
                // 未处理、处理中和挂起的步骤才需要计算SLA
                if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())
                    || processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())
                    || processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
                } else {
                    it.remove();
                }
            }
            ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
            ProcessTaskSlaVo processTaskSlaVo = processTaskMapper.getProcessTaskSlaById(slaId);
            /** 存在未完成步骤才发超时通知，否则清除通知作业 **/
            if (processTaskSlaVo != null && processTaskSlaTimeVo != null
                && MapUtils.isNotEmpty(processTaskSlaNotifyVo.getConfigObj()) && processTaskStepList.size() > 0) {
                JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
                JSONObject notifyPolicyConfig = policyObj.getJSONObject("notifyPolicyConfig");
                if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                    Long policyId = notifyPolicyConfig.getLong("policyId");
                    NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
                    if (notifyPolicyVo != null) {
                        List<ParamMappingVo> paramMappingList =
                            JSON.parseArray(JSON.toJSONString(notifyPolicyConfig.getJSONArray("paramMappingList")),
                                ParamMappingVo.class);
                        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskSlaVo.getProcessTaskId());
                        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
                        JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
                        JSONObject templateParamData = ProcessTaskUtil.getProcessTaskParamData(processTaskVo);
                        Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                            processTaskService.getReceiverMap(processTaskStepVo, receiverMap);
                        }
                        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                        processTaskStepVo.setProcessTaskId(processTaskSlaVo.getProcessTaskId());
                        NotifyPolicyUtil.execute(notifyPolicyVo.getHandler(), SlaNotifyTriggerType.TIMEOUT, ProcessTaskMessageHandler.class, notifyPolicyVo.getConfig(), paramMappingList,
                                templateParamData, conditionParamData, receiverMap, processTaskStepVo);
                    }
                }
                Date nextFireTime = context.getNextFireTime();
                if (nextFireTime != null) {
                    processTaskSlaNotifyVo.setTriggerTime(nextFireTime);
                    processTaskMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
                } else {
                    // 删除通知记录
                    processTaskMapper.deleteProcessTaskSlaNotifyById(processTaskSlaNotifyVo.getId());
                }
            } else {
                schedulerManager.unloadJob(jobObject);
                if (processTaskSlaNotifyVo != null) {
                    // 删除通知记录
                    processTaskMapper.deleteProcessTaskSlaNotifyById(processTaskSlaNotifyVo.getId());
                }
            }
        } else {
            schedulerManager.unloadJob(jobObject);
        }
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-NOTIFY";
    }

}
