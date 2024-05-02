/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.util.NotifyPolicyUtil;
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
        super("PROCESSTASK-NOTIFY" + (_trigger != null ? "-" + _trigger.getTrigger() : "") + (_currentProcessTaskStepVo != null ? "-" + _currentProcessTaskStepVo.getId() : ""));
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        notifyTriggerType = _trigger;
    }

    @Override
    protected void execute() {
        try {

            INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
            StringBuilder notifyAuditMessageStringBuilder = new StringBuilder();
            JSONObject notifyPolicyConfig;
            if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
                /* 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(config, "process.processConfig.notifyPolicyConfig");
                notifyAuditMessageStringBuilder.append(currentProcessTaskStepVo.getProcessTaskId());
            } else {
                /* 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(stepVo.getHandler());
                if (processStepUtilHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                }
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(stepConfig, "notifyPolicyConfig");

                notifyAuditMessageStringBuilder.append(stepVo.getProcessTaskId());
                notifyAuditMessageStringBuilder.append("-");
                notifyAuditMessageStringBuilder.append(stepVo.getName());
                notifyAuditMessageStringBuilder.append("(");
                notifyAuditMessageStringBuilder.append(stepVo.getId());
                notifyAuditMessageStringBuilder.append(")");
            }

            InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig);
            if (invokeNotifyPolicyConfigVo == null) {
                return;
            }
            // 触发点被排除，不用发送邮件
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
