/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.*;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.util.NotifyPolicyUtil;
import neatlogic.module.process.message.handler.ProcessTaskMessageHandler;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
        super("PROCESSTASK-NOTIFY" + (_currentProcessTaskStepVo != null ? "-" + _currentProcessTaskStepVo.getId() : ""));
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        notifyTriggerType = _trigger;
    }

    @Override
    protected void execute() {
        try {

            INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
            StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
            InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = null;
            if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
                /** 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                JSONObject notifyPolicyConfig = (JSONObject) JSONPath.read(config, "process.processConfig.notifyPolicyConfig");
                invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig);
                notifyAuditMessageStringBuilder.append(currentProcessTaskStepVo.getProcessTaskId());
            } else {
                /* 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(stepVo.getHandler());
                if (processStepUtilHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                }
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                JSONObject notifyPolicyConfig = (JSONObject) JSONPath.read(stepConfig, "notifyPolicyConfig");
                invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig);

                notifyAuditMessageStringBuilder.append(stepVo.getProcessTaskId());
                notifyAuditMessageStringBuilder.append("-");
                notifyAuditMessageStringBuilder.append(stepVo.getName());
                notifyAuditMessageStringBuilder.append("(");
                notifyAuditMessageStringBuilder.append(stepVo.getId());
                notifyAuditMessageStringBuilder.append(")");
            }

            if (invokeNotifyPolicyConfigVo == null) {
                return;
            }
            List<String> excludeTriggerList = invokeNotifyPolicyConfigVo.getExcludeTriggerList();
            if (CollectionUtils.isNotEmpty(excludeTriggerList) && excludeTriggerList.contains(notifyTriggerType.getTrigger())) {
                return;
            }

            Long policyId = invokeNotifyPolicyConfigVo.getPolicyId();
            if (policyId == null) {
                return;
            }
            NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
            if (notifyPolicyVo == null || notifyPolicyVo.getConfig() == null) {
                return;
            }
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
            NotifyPolicyUtil.execute(notifyPolicyHandler, notifyTriggerType, ProcessTaskMessageHandler.class, notifyPolicyVo, paramMappingList, conditionParamData, receiverMap, currentProcessTaskStepVo, fileList, notifyAuditMessageStringBuilder.toString());
        } catch (Exception ex) {
            logger.error("通知失败：" + ex.getMessage(), ex);
        }
    }

}
