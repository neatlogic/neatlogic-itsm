/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.schedule.plugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.util.NotifyPolicyUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.message.handler.ProcessTaskMessageHandler;
import neatlogic.module.process.notify.constvalue.SlaNotifyTriggerType;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@DisallowConcurrentExecution
public class ProcessTaskSlaNotifyJob extends JobBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskSlaNotifyJob.class);

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
        boolean flag = false;
        StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
        Long slaNotifyId = null;
        try {
            notifyAuditMessageStringBuilder.append("触发点为 ").append(SlaNotifyTriggerType.TIMEOUT.getTrigger()).append("(").append(SlaNotifyTriggerType.TIMEOUT.getText()).append(")");
            slaNotifyId = Long.valueOf(jobObject.getJobName());
            ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskSlaMapper.getProcessTaskSlaNotifyById(slaNotifyId);
            if (processTaskSlaNotifyVo != null) {
                notifyAuditMessageStringBuilder.append(" slaNotifyId为 ").append(slaNotifyId);
                Long slaId = processTaskSlaNotifyVo.getSlaId();
                ProcessTaskSlaVo processTaskSlaVo = processTaskSlaMapper.getProcessTaskSlaById(slaId);
                if (processTaskSlaVo != null) {
                    notifyAuditMessageStringBuilder.append(" 时效为 ").append(processTaskSlaVo.getId()).append("(").append(processTaskSlaVo.getName()).append(")");
                    ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(slaId);
                    if (processTaskSlaTimeVo != null) {
                        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskSlaVo.getProcessTaskId());
                        if (processTaskVo != null) {
                            notifyAuditMessageStringBuilder.append(" 工单为 ").append(processTaskVo.getTitle()).append("(").append(processTaskVo.getId()).append(")");
                            JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
                            if (MapUtils.isNotEmpty(policyObj)) {
                                // 如果是超时前触发通知，当前时间已经超过了超时时间点，则不再发送通知
                                String expression = policyObj.getString("expression");
                                if (!expression.equalsIgnoreCase("before") || new Date().before(processTaskSlaTimeVo.getExpireTime())) {
                                    JSONObject notifyPolicyConfig = policyObj.getJSONObject("notifyPolicyConfig");
                                    if (notifyPolicyConfig != null) {
                                        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = JSON.toJavaObject(notifyPolicyConfig, InvokeNotifyPolicyConfigVo.class);
                                        // 触发点被排除，不用发送邮件
                                        List<String> excludeTriggerList = invokeNotifyPolicyConfigVo.getExcludeTriggerList();
                                        if (CollectionUtils.isNotEmpty(excludeTriggerList) && excludeTriggerList.contains(SlaNotifyTriggerType.TIMEOUT.getTrigger())) {
                                            notifyAuditMessageStringBuilder.append(" 通知策略设置触发时机中排除了触发点").append(SlaNotifyTriggerType.TIMEOUT.getTrigger()).append("(").append(SlaNotifyTriggerType.TIMEOUT.getText()).append(")");
                                        } else {
                                            NotifyPolicyVo notifyPolicyVo = null;
                                            if (invokeNotifyPolicyConfigVo.getIsCustom() == 1) {
                                                notifyAuditMessageStringBuilder.append(" 设置通知策略ID为 ");
                                                if (invokeNotifyPolicyConfigVo.getPolicyId() != null) {
                                                    notifyAuditMessageStringBuilder.append(invokeNotifyPolicyConfigVo.getPolicyId());
                                                    notifyPolicyVo = notifyMapper.getNotifyPolicyById(invokeNotifyPolicyConfigVo.getPolicyId());
                                                    if (notifyPolicyVo == null) {
                                                        notifyAuditMessageStringBuilder.append("，但是该通知策略不存在");
                                                    } else {
                                                        notifyAuditMessageStringBuilder.append("，找到默认通知策略").append(notifyPolicyVo.getName()).append("(").append(notifyPolicyVo.getId()).append(")");
                                                    }
                                                } else {
                                                    notifyAuditMessageStringBuilder.append("null");
                                                }
                                            } else {
                                                notifyAuditMessageStringBuilder.append(" 没有设置通知策略");
                                                if (invokeNotifyPolicyConfigVo.getHandler() != null) {
                                                    notifyAuditMessageStringBuilder.append(" 通过通知策略handler=").append(invokeNotifyPolicyConfigVo.getHandler());
                                                    notifyPolicyVo = notifyMapper.getDefaultNotifyPolicyByHandler(invokeNotifyPolicyConfigVo.getHandler());
                                                    if (notifyPolicyVo == null) {
                                                        notifyAuditMessageStringBuilder.append(" ，找不到默认通知策略");
                                                    } else {
                                                        notifyAuditMessageStringBuilder.append(" ，找到默认通知策略 ").append(notifyPolicyVo.getName()).append("(").append(notifyPolicyVo.getId()).append(")");
                                                    }
                                                } else {
                                                    notifyAuditMessageStringBuilder.append(" 由于通知策略handler为null，无法找到默认通知策略");
                                                }
                                            }
                                            if (notifyPolicyVo != null) {
                                                if (notifyPolicyVo.getConfig() != null) {
                                                    List<ParamMappingVo> paramMappingList = invokeNotifyPolicyConfigVo.getParamMappingList();
                                                    List<FileVo> fileList = processTaskMapper.getFileListByProcessTaskId(processTaskSlaVo.getProcessTaskId());
                                                    if (CollectionUtils.isNotEmpty(fileList)) {
                                                        fileList = fileList.stream().filter(o -> o.getSize() <= 10 * 1024 * 1024).collect(Collectors.toList());
                                                    }
                                                    List<ProcessTaskStepVo> needNotifyStepList = new ArrayList<>();
                                                    List<Long> processTaskStepIdList = processTaskSlaMapper.getProcessTaskStepIdListBySlaId(slaId);
                                                    if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                                                        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                                                        if (CollectionUtils.isNotEmpty(processTaskStepList)) {
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
                                                        }
                                                    }

                                                    /** 存在未完成步骤才发超时通知，否则清除通知作业 **/
                                                    if (CollectionUtils.isNotEmpty(needNotifyStepList)) {
                                                        flag = true;
                                                        for (ProcessTaskStepVo processTaskStepVo : needNotifyStepList) {
                                                            JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), processTaskStepVo);
                                                            Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                                                            processTaskService.getReceiverMap(processTaskStepVo, receiverMap, SlaNotifyTriggerType.TIMEOUT);
                                                            String notifyAuditMessage = notifyAuditMessageStringBuilder + " 步骤为 " + processTaskStepVo.getName() + "(" + processTaskStepVo.getId() + ")";
                                                            NotifyPolicyUtil.execute(
                                                                    notifyPolicyVo.getHandler(),
                                                                    SlaNotifyTriggerType.TIMEOUT,
                                                                    ProcessTaskMessageHandler.class,
                                                                    notifyPolicyVo, paramMappingList,
                                                                    conditionParamData,
                                                                    receiverMap,
                                                                    processTaskStepVo,
                                                                    fileList,
                                                                    notifyAuditMessage
                                                            );
                                                        }
                                                        Date nextFireTime = context.getNextFireTime();
                                                        if (nextFireTime != null) {
                                                            processTaskSlaNotifyVo.setTriggerTime(nextFireTime);
                                                            processTaskSlaMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
                                                        } else {
                                                            schedulerManager.unloadJob(jobObject);
                                                            // 删除通知记录
                                                            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
                                                        }
                                                    } else {
                                                        notifyAuditMessageStringBuilder.append(" 没有需要通知的步骤，不触发通知");
                                                    }
                                                } else {
                                                    notifyAuditMessageStringBuilder.append(" 通知策略config为null，不触发通知");
                                                }
                                            }
                                        }
                                    } else {
                                        notifyAuditMessageStringBuilder.append(" notifyPolicyConfig为null，不触发通知");
                                    }
                                } else {
                                    notifyAuditMessageStringBuilder.append(" 设置超时前触发通知，当前时间已经超过了超时时间点，不触发通知");
                                }
                            } else {
                                notifyAuditMessageStringBuilder.append(" 数据库表`processtask_sla_notify`中`id`=").append(slaId).append("的数据`config`为").append(policyObj).append("，不触发通知");
                            }
                        } else {
                            notifyAuditMessageStringBuilder.append(" 数据库表`processtask`中没有`id`=").append(slaId).append("的数据，不触发通知");
                        }
                    } else {
                        notifyAuditMessageStringBuilder.append(" 数据库表`processtask_sla_time`中没有`sla_id`=").append(slaId).append("的数据，不触发通知");
                    }
                } else {
                    notifyAuditMessageStringBuilder.append(" 数据库表`processtask_sla`中没有`id`=").append(slaId).append("的数据，不触发通知");
                }
            } else {
                notifyAuditMessageStringBuilder.append(" 数据库表`processtask_sla_notify`中没有`id`=").append(slaNotifyId).append("的数据，不触发通知");
            }
        } catch (Exception ex) {
            logger.error(notifyAuditMessageStringBuilder + " 通知失败：{}", ex.getMessage(), ex);
        } finally {
            if (!flag) {
                schedulerManager.unloadJob(jobObject);
                processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
                Logger notifyAuditLogger = LoggerFactory.getLogger("notifyAudit");
                notifyAuditLogger.info("\n" + notifyAuditMessageStringBuilder.toString());
            }
        }
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-NOTIFY";
    }

}
