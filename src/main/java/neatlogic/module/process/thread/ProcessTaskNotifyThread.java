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

package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.util.NotifyPolicyUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.message.handler.ProcessTaskMessageHandler;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessTaskNotifyThread extends NeatLogicThread {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static NotifyMapper notifyMapper;
    private static ProcessTaskService processTaskService;

    @Autowired
    public void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
    }

    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Autowired
    public void setSelectContentByHashMapper(SelectContentByHashMapper _selectContentByHashMapper) {
        selectContentByHashMapper = _selectContentByHashMapper;
    }

    @Autowired
    public void setNotifyMapper(NotifyMapper _notifyMapper) {
        notifyMapper = _notifyMapper;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private INotifyTriggerType notifyTriggerType;

    public ProcessTaskNotifyThread() {
        super("PROCESSTASK-NOTIFY");
    }

    public ProcessTaskNotifyThread(ProcessTaskStepVo _currentProcessTaskStepVo, INotifyTriggerType _trigger) {
        super("PROCESSTASK-NOTIFY" + (_trigger != null ? "-" + _trigger.getTrigger() : "") + (_currentProcessTaskStepVo.getId() != null ? "-" + _currentProcessTaskStepVo.getId() : (_currentProcessTaskStepVo.getProcessTaskId() != null ? "-" + _currentProcessTaskStepVo.getProcessTaskId(): "")));
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        notifyTriggerType = _trigger;
    }

    @Override
    protected void execute() {
        boolean flag = false;
        StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
        try {
            notifyAuditMessageStringBuilder.append("触发点为 ").append(notifyTriggerType.getTrigger()).append("(").append(notifyTriggerType.getText()).append(")");
            JSONObject notifyPolicyConfig;
            if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
                /* 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                notifyAuditMessageStringBuilder.append(" 工单为 ").append(processTaskVo.getId()).append("(").append(processTaskVo.getTitle()).append(")");
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(config, "process.processConfig.notifyPolicyConfig");
                if (notifyPolicyConfig == null) {
                    notifyAuditMessageStringBuilder.append(" process.processConfig.notifyPolicyConfig为null");
                }
            } else {
                /* 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(stepVo.getHandler());
                if (processStepUtilHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                }
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                notifyAuditMessageStringBuilder.append(" 工单为 ").append(processTaskVo.getTitle()).append("(").append(processTaskVo.getId()).append(")");
                notifyAuditMessageStringBuilder.append(" 步骤为 ").append(stepVo.getName()).append("(").append(stepVo.getId()).append(")");
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(stepConfig, "notifyPolicyConfig");
                if (notifyPolicyConfig == null) {
                    notifyAuditMessageStringBuilder.append(" notifyPolicyConfig为null");
                }
                currentProcessTaskStepVo.setProcessTaskId(stepVo.getProcessTaskId());
                currentProcessTaskStepVo.setName(stepVo.getName());
                currentProcessTaskStepVo.setProcessStepUuid(stepVo.getProcessStepUuid());
                currentProcessTaskStepVo.setStatus(stepVo.getStatus());
                currentProcessTaskStepVo.setType(stepVo.getType());
                currentProcessTaskStepVo.setHandler(stepVo.getHandler());
                currentProcessTaskStepVo.setIsActive(stepVo.getIsActive());
                currentProcessTaskStepVo.setConfigHash(stepVo.getConfigHash());
                currentProcessTaskStepVo.setActiveTime(stepVo.getActiveTime());
                currentProcessTaskStepVo.setStartTime(stepVo.getStartTime());
                currentProcessTaskStepVo.setEndTime(stepVo.getEndTime());
                currentProcessTaskStepVo.setError(stepVo.getError());
            }
            if (notifyPolicyConfig != null) {
                InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = JSON.toJavaObject(notifyPolicyConfig, InvokeNotifyPolicyConfigVo.class);
                // 触发点被排除，不用发送邮件
                List<String> excludeTriggerList = invokeNotifyPolicyConfigVo.getExcludeTriggerList();
                if (CollectionUtils.isNotEmpty(excludeTriggerList) && excludeTriggerList.contains(notifyTriggerType.getTrigger())) {
                    notifyAuditMessageStringBuilder.append(" 通知策略设置触发时机中排除了触发点").append(notifyTriggerType.getTrigger()).append("(").append(notifyTriggerType.getText()).append(")");
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
                        notifyAuditMessageStringBuilder.append(" 没有设置通知策略 ");
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
                            JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), currentProcessTaskStepVo);
                            Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                            processTaskService.getReceiverMap(currentProcessTaskStepVo, receiverMap, notifyTriggerType);
                            /* 参数映射列表 **/
                            List<ParamMappingVo> paramMappingList = invokeNotifyPolicyConfigVo.getParamMappingList();
                            List<FileVo> fileList = processTaskMapper.getFileListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                            if (CollectionUtils.isNotEmpty(fileList)) {
                                fileList = fileList.stream().filter(o -> o.getSize() <= 10 * 1024 * 1024).collect(Collectors.toList());
                            }
                            String notifyPolicyHandler = notifyPolicyVo.getHandler();
                            flag = true;
                            NotifyPolicyUtil.execute(notifyPolicyHandler, notifyTriggerType, ProcessTaskMessageHandler.class, notifyPolicyVo, paramMappingList, conditionParamData, receiverMap, currentProcessTaskStepVo, fileList, notifyAuditMessageStringBuilder.toString());
                        } else {
                            notifyAuditMessageStringBuilder.append(" 通知策略config为null，不触发通知");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(notifyAuditMessageStringBuilder + " 通知失败：{}", ex.getMessage(), ex);
        } finally {
            if (!flag) {
                Logger notifyAuditLogger = LoggerFactory.getLogger("notifyAudit");
                notifyAuditLogger.info("\n" + notifyAuditMessageStringBuilder);
            }
        }
    }

}
