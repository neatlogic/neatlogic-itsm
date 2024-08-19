/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.schedule.plugin;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.util.NotifyPolicyUtil;
import neatlogic.module.process.message.handler.ProcessTaskMessageHandler;
import neatlogic.module.process.notify.constvalue.SlaNotifyTriggerType;
import neatlogic.module.process.service.ProcessTaskService;
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
        if (processTaskSlaNotifyVo == null) {
            schedulerManager.unloadJob(jobObject);
            return;
//            System.out.println("由于processTaskSlaNotifyVo为null，卸载job");
        }
        JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
        /** 存在未完成步骤才发超时通知，否则清除通知作业 **/
        if (MapUtils.isEmpty(policyObj)) {
            schedulerManager.unloadJob(jobObject);
            // 删除通知记录
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
//                System.out.println("由于不满足执行条件，卸载job");
        }
        Long slaId = processTaskSlaNotifyVo.getSlaId();
        ProcessTaskSlaVo processTaskSlaVo = processTaskSlaMapper.getProcessTaskSlaById(slaId);
        if (processTaskSlaVo == null) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskSlaVo.getProcessTaskId());
        if (processTaskVo == null) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(slaId);
        if (processTaskSlaTimeVo == null) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        // 如果是超时前触发通知，当前时间已经超过了超时时间点，则不再发送通知
        String expression = policyObj.getString("expression");
        if (expression.equalsIgnoreCase("before") && new Date().after(processTaskSlaTimeVo.getExpireTime())) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        List<Long> processTaskStepIdList = processTaskSlaMapper.getProcessTaskStepIdListBySlaId(slaId);
        if (CollectionUtils.isEmpty(processTaskStepIdList)) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
        if (CollectionUtils.isEmpty(processTaskStepList)) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        List<ProcessTaskStepVo> needNotifyStepList = new ArrayList<>();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            // 未处理、处理中和挂起的步骤才需要发送通知
            if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                if (processTaskStepVo.getStatus().equals(ProcessTaskStepStatus.PENDING.getValue())) {
                    needNotifyStepList.add(processTaskStepVo);
                }
                if (processTaskStepVo.getStatus().equals(ProcessTaskStepStatus.RUNNING.getValue())) {
                    needNotifyStepList.add(processTaskStepVo);
                }
                if (processTaskStepVo.getStatus().equals(ProcessTaskStepStatus.HANG.getValue())) {
                    needNotifyStepList.add(processTaskStepVo);
                }
            }
        }
        if (CollectionUtils.isEmpty(needNotifyStepList)) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
//            System.out.println("时效id=" + slaId);
//            System.out.println("时效name=" + processTaskSlaVo.getName());
        JSONObject notifyPolicyConfig = policyObj.getJSONObject("notifyPolicyConfig");
        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig);
        if (invokeNotifyPolicyConfigVo == null) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        // 触发点被排除，不用发送邮件
        List<String> excludeTriggerList = invokeNotifyPolicyConfigVo.getExcludeTriggerList();
        if (CollectionUtils.isNotEmpty(excludeTriggerList) && excludeTriggerList.contains(SlaNotifyTriggerType.TIMEOUT.getTrigger())) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        Long policyId = invokeNotifyPolicyConfigVo.getPolicyId();
        if (policyId == null) {
            return;
        }
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null || notifyPolicyVo.getConfig() == null) {
            schedulerManager.unloadJob(jobObject);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
            return;
        }
        List<ParamMappingVo> paramMappingList = invokeNotifyPolicyConfigVo.getParamMappingList();

        List<FileVo> fileList = processTaskMapper.getFileListByProcessTaskId(processTaskSlaVo.getProcessTaskId());
        if (CollectionUtils.isNotEmpty(fileList)) {
            fileList = fileList.stream().filter(o -> o.getSize() <= 10 * 1024 * 1024).collect(Collectors.toList());
        }
        for (ProcessTaskStepVo processTaskStepVo : needNotifyStepList) {
            JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), processTaskStepVo);
            Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
            processTaskService.getReceiverMap(processTaskStepVo, receiverMap, SlaNotifyTriggerType.TIMEOUT);

            StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
            notifyAuditMessageStringBuilder.append(processTaskSlaVo.getProcessTaskId());
            notifyAuditMessageStringBuilder.append("-");
            notifyAuditMessageStringBuilder.append(processTaskStepVo.getName());
            notifyAuditMessageStringBuilder.append("(");
            notifyAuditMessageStringBuilder.append(processTaskStepVo.getId());
            notifyAuditMessageStringBuilder.append(")");
            NotifyPolicyUtil.execute(notifyPolicyVo.getHandler(), SlaNotifyTriggerType.TIMEOUT, ProcessTaskMessageHandler.class, notifyPolicyVo, paramMappingList,
                    conditionParamData, receiverMap, processTaskStepVo, fileList, notifyAuditMessageStringBuilder.toString());
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
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-NOTIFY";
    }

}
