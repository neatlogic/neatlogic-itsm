/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.schedule.plugin;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskSlaMapper;
import neatlogic.framework.process.dto.ProcessTaskSlaNotifyVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskSlaVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.util.NotifyPolicyUtil;
import neatlogic.module.process.message.handler.ProcessTaskMessageHandler;
import neatlogic.module.process.notify.constvalue.SlaNotifyTriggerType;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@DisallowConcurrentExecution
public class ProcessTaskSlaNotifyJob extends JobBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private NotifyMapper notifyMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public Boolean isMyHealthy(JobObject jobObject) {
        Long slaNotifyId = Long.valueOf(jobObject.getJobName());
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskSlaMapper.getProcessTaskSlaNotifyById(slaNotifyId);
        if (processTaskSlaNotifyVo == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void reloadJob(JobObject jobObject) {
//        System.out.println("开始加载sla通知策略job");
        String tenantUuid = jobObject.getTenantUuid();
        TenantContext.get().switchTenant(tenantUuid);
        Long slaNotifyId = Long.valueOf(jobObject.getJobName());
//        System.out.println("slaNotifyId=" + slaNotifyId);
//        ProcessTaskSlaVo processTaskSlaVo = processTaskMapper.getProcessTaskSlaById(processTaskSlaNotifyVo.getSlaId());
//        System.out.println("时效id=" + processTaskSlaNotifyVo.getSlaId());
//        System.out.println("时效name=" + processTaskSlaVo.getName());
        boolean isJobLoaded = false;
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskSlaMapper.getProcessTaskSlaNotifyById(slaNotifyId);
        try {
            if (processTaskSlaNotifyVo != null) {
                ProcessTaskSlaTimeVo slaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(processTaskSlaNotifyVo.getSlaId());
                if (slaTimeVo != null) {
                    JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
                    if (MapUtils.isNotEmpty(policyObj)) {
                        String expression = policyObj.getString("expression");
                        int time = policyObj.getIntValue("time");
                        String unit = policyObj.getString("unit");
                        String executeType = policyObj.getString("executeType");
                        int intervalTime = policyObj.getIntValue("intervalTime");
                        Integer repeatCount = null;
                        if ("loop".equals(executeType) && intervalTime > 0) {// 周期执行
                            String intervalUnit = policyObj.getString("intervalUnit");
                            if (intervalUnit.equalsIgnoreCase("day")) {
                                intervalTime = intervalTime * 24 * 60 * 60;
                            } else if (intervalUnit.equalsIgnoreCase("hour")) {
                                intervalTime = intervalTime * 60 * 60;
                            } else {
                                intervalTime = intervalTime * 60;
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
//                            System.out.println("触发时间在当前时间之前");
                            if ("loop".equals(executeType)) {
                                /** 如果是循环触发，则将触发时间改为当前时间 **/
                                notifyDate = Calendar.getInstance();
                            } else {
                                /** 如果是单次触发，不启动作业 **/
                                return;
                            }
                        }
                        JobObject.Builder newJobObjectBuilder = new JobObject.Builder(
                                slaNotifyId.toString(),
                                this.getGroupName(),
                                this.getClassName(),
                                TenantContext.get().getTenantUuid()
                        ).withBeginTime(notifyDate.getTime())
                                .withIntervalInSeconds(intervalTime)
                                .withRepeatCount(repeatCount);
                        JobObject newJobObject = newJobObjectBuilder.build();
                        Date triggerDate = schedulerManager.loadJob(newJobObject);
                        if (triggerDate != null) {
                            // 更新通知记录时间
                            processTaskSlaNotifyVo.setTriggerTime(triggerDate);
                            processTaskSlaMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
                            isJobLoaded = true;
//                            System.out.println("加载成功，triggerDate：" + triggerDate);
                        }
                    }
                }
            }
        } finally {
            if (!isJobLoaded) {
//                System.out.println("加载失败");
                // 没有加载到作业，则删除通知记录
                processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            }
        }
    }

    @Override
    public void initJob(String tenantUuid) {
        List<ProcessTaskSlaNotifyVo> slaNotifyList = processTaskSlaMapper.getAllProcessTaskSlaNotify();
        for (ProcessTaskSlaNotifyVo processTaskSlaNotifyVo : slaNotifyList) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    processTaskSlaNotifyVo.getId().toString(),
                    this.getGroupName(),
                    this.getClassName(),
                    TenantContext.get().getTenantUuid()
            );
            JobObject jobObject = jobObjectBuilder.build();
//            System.out.println("initJob....");
            this.reloadJob(jobObject);
        }
    }

    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {
//        System.out.println("开始执行sla通知策略逻辑...");
        Long slaNotifyId = Long.valueOf(jobObject.getJobName());
//        System.out.println("slaNotifyId=" + slaNotifyId);
        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskSlaMapper.getProcessTaskSlaNotifyById(slaNotifyId);
        if (processTaskSlaNotifyVo != null) {
            Long slaId = processTaskSlaNotifyVo.getSlaId();
            List<ProcessTaskStepVo> needNotifyStepList = new ArrayList<>();
            List<Long> processTaskStepIdList = processTaskSlaMapper.getProcessTaskStepIdListBySlaId(slaId);
            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    // 未处理、处理中和挂起的步骤才需要发送通知
                    if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                        if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
                            needNotifyStepList.add(processTaskStepVo);
                        }
                        if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
                            needNotifyStepList.add(processTaskStepVo);
                        }
                        if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
                            needNotifyStepList.add(processTaskStepVo);
                        }
                    }
                }
            }
//            Iterator<ProcessTaskStepVo> it = processTaskStepList.iterator();
//            while (it.hasNext()) {
//                ProcessTaskStepVo processTaskStepVo = it.next();
//                // 未处理、处理中和挂起的步骤才需要计算SLA
//                if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())
//                        || processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())
//                        || processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
//                } else {
//                    it.remove();
//                }
//            }
            ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(slaId);
            ProcessTaskSlaVo processTaskSlaVo = processTaskSlaMapper.getProcessTaskSlaById(slaId);
//            System.out.println("时效id=" + slaId);
//            System.out.println("时效name=" + processTaskSlaVo.getName());
            /** 存在未完成步骤才发超时通知，否则清除通知作业 **/
            if (processTaskSlaVo != null && processTaskSlaTimeVo != null
                    && MapUtils.isNotEmpty(processTaskSlaNotifyVo.getConfigObj()) && needNotifyStepList.size() > 0) {
                JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
                JSONObject notifyPolicyConfig = policyObj.getJSONObject("notifyPolicyConfig");
//                System.out.println("发送通知");
                if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                    Long policyId = notifyPolicyConfig.getLong("policyId");
                    NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
                    if (notifyPolicyVo != null) {
                        List<ParamMappingVo> paramMappingList = new ArrayList<>();
                        JSONArray paramMappingArray = notifyPolicyConfig.getJSONArray("paramMappingList");
                        if (CollectionUtils.isNotEmpty(paramMappingArray)) {
                            paramMappingList = paramMappingArray.toJavaList(ParamMappingVo.class);
                        }
                        StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
                        notifyAuditMessageStringBuilder.append(processTaskSlaVo.getProcessTaskId());
                        ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
                        processTaskStep.setIsAutoGenerateId(false);
                        processTaskStep.setProcessTaskId(processTaskSlaVo.getProcessTaskId());
                        JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), processTaskStep);
//                        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskSlaVo.getProcessTaskId());
//                        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
//                        JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
//                        JSONObject templateParamData = ProcessTaskUtil.getProcessTaskParamData(processTaskVo);
                        List<Long> stepIdList = new ArrayList<>();
                        List<String> stepNameList = new ArrayList<>();
                        Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                        for (ProcessTaskStepVo processTaskStepVo : needNotifyStepList) {
                            processTaskService.getReceiverMap(processTaskStepVo, receiverMap, SlaNotifyTriggerType.TIMEOUT);
                            stepIdList.add(processTaskStepVo.getId());
                            stepNameList.add(processTaskStepVo.getName());
                            notifyAuditMessageStringBuilder.append("-");
                            notifyAuditMessageStringBuilder.append(processTaskStepVo.getName());
                            notifyAuditMessageStringBuilder.append("(");
                            notifyAuditMessageStringBuilder.append(processTaskStepVo.getId());
                            notifyAuditMessageStringBuilder.append(")");
                        }
                        JSONObject paramObj = processTaskStep.getParamObj();
                        paramObj.put("idList", stepIdList);
                        paramObj.put("nameList", stepNameList);
                        List<FileVo> fileList = processTaskMapper.getFileListByProcessTaskId(processTaskSlaVo.getProcessTaskId());
                        if (CollectionUtils.isNotEmpty(fileList)) {
                            fileList = fileList.stream().filter(o -> o.getSize() <= 10 * 1024 * 1024).collect(Collectors.toList());
                        }
                        NotifyPolicyUtil.execute(notifyPolicyVo.getHandler(), SlaNotifyTriggerType.TIMEOUT, ProcessTaskMessageHandler.class, notifyPolicyVo, paramMappingList,
                                conditionParamData, receiverMap, processTaskStep, fileList, notifyAuditMessageStringBuilder.toString());
                    }
                }
                Date nextFireTime = context.getNextFireTime();
                if (nextFireTime != null) {
                    processTaskSlaNotifyVo.setTriggerTime(nextFireTime);
                    processTaskSlaMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
                } else {
                    // 删除通知记录
                    processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
//                    System.out.println("由于不需要下次执行，删除processTaskSlaNotify");
                }
            } else {
                schedulerManager.unloadJob(jobObject);
                // 删除通知记录
                processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
//                System.out.println("由于不满足执行条件，卸载job");
            }
        } else {
            schedulerManager.unloadJob(jobObject);
//            System.out.println("由于processTaskSlaNotifyVo为null，卸载job");
        }
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-NOTIFY";
    }

}