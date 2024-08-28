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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationResultVo;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.dto.ParamMappingVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskParams;
import neatlogic.framework.process.dto.ProcessTaskActionVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.handler.ProcessRequestFrom;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.util.ConditionUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskActionMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessTaskActionThread extends NeatLogicThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static ProcessTaskActionMapper processTaskActionMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static IntegrationMapper integrationMapper;

    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Autowired
    public void setProcessTaskActionMapper(ProcessTaskActionMapper _processTaskActionMapper) {
        processTaskActionMapper = _processTaskActionMapper;
    }

    @Autowired
    public void setSelectContentByHashMapper(SelectContentByHashMapper _selectContentByHashMapper) {
        selectContentByHashMapper = _selectContentByHashMapper;
    }

    @Autowired
    public void setIntegrationMapper(IntegrationMapper _integrationMapper) {
        integrationMapper = _integrationMapper;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private INotifyTriggerType triggerType;

    public ProcessTaskActionThread() {
        super("PROCESSTASK-ACTION-HANDLER");
    }

    public ProcessTaskActionThread(ProcessTaskStepVo _currentProcessTaskStepVo, INotifyTriggerType _trigger) {
        super("PROCESSTASK-ACTION-HANDLER" + (_currentProcessTaskStepVo != null ? "-" + _currentProcessTaskStepVo.getId() : ""));
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        triggerType = _trigger;
    }

    @Override
    protected void execute() {
        try {
            JSONArray actionList = null;
            if (triggerType instanceof ProcessTaskNotifyTriggerType) {
                /* 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                actionList = (JSONArray) JSONPath.read(config, "process.processConfig.actionConfig.actionList");
            } else {
                /* 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                actionList = (JSONArray) JSONPath.read(stepConfig, "actionConfig.actionList");
            }
            /* 从步骤配置信息中获取动作列表 **/
            if (CollectionUtils.isNotEmpty(actionList)) {
                List<String> processTaskParams = Arrays.stream(ProcessTaskParams.values()).map(ProcessTaskParams::getValue).collect(Collectors.toList());
                for (int i = 0; i < actionList.size(); i++) {
                    JSONObject actionObj = actionList.getJSONObject(i);
                    if (triggerType.getTrigger().equals(actionObj.getString("trigger"))) {
                        String integrationUuid = actionObj.getString("integrationUuid");
                        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
                        if (integrationVo == null) {
                            throw new IntegrationNotFoundException(integrationUuid);
                        }
                        IIntegrationHandler iIntegrationHandler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
                        if (iIntegrationHandler == null) {
                            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
                        }
                        String failedReason = null;
                        JSONObject config = new JSONObject();
                        /** 参数映射 **/
                        JSONObject integrationParam = new JSONObject();
                        JSONArray paramMappingArray = actionObj.getJSONArray("paramMappingList");
                        if (CollectionUtils.isNotEmpty(paramMappingArray)) {
                            JSONObject processFieldData = ProcessTaskConditionFactory.getConditionParamData(processTaskParams, currentProcessTaskStepVo);
                            List<ParamMappingVo> paramMappingList = paramMappingArray.toJavaList(ParamMappingVo.class);
                            for (ParamMappingVo paramMappingVo : paramMappingList) {
                                if (ProcessFieldType.CONSTANT.getValue().equals(paramMappingVo.getType())) {
                                    integrationParam.put(paramMappingVo.getName(), paramMappingVo.getValue());
                                } else if (StringUtils.isNotBlank(paramMappingVo.getType())) {
                                    Object processFieldValue = processFieldData.get(paramMappingVo.getValue());
                                    if (processFieldValue != null) {
                                        integrationParam.put(paramMappingVo.getName(), processFieldValue);
                                    } else {
                                        logger.error("没有找到参数'" + paramMappingVo.getValue() + "'信息");
                                    }
                                }
                            }
                        }
                        integrationParam.put("triggerType", triggerType.getTrigger());
                        integrationVo.getParamObj().putAll(integrationParam);
                        config.put("param", integrationParam);
                        boolean isSucceed = false;
                        IntegrationResultVo integrationResultVo = iIntegrationHandler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
                        if (StringUtils.isNotBlank(integrationResultVo.getError())) {
                            logger.error(integrationResultVo.getError());
                        } else {
                            JSONObject successConditionObj = actionObj.getJSONObject("successCondition");
                            if (MapUtils.isNotEmpty(successConditionObj)) {
                                String name = successConditionObj.getString("name");
                                if (StringUtils.isNotBlank(name)) {
                                    String resultValue = null;
                                    String transformedResult = integrationResultVo.getTransformedResult();
                                    if (StringUtils.isNotBlank(transformedResult)) {
                                        config.put("result", transformedResult);
                                        JSONObject transformedResultObj = JSON.parseObject(transformedResult);
                                        if (MapUtils.isNotEmpty(transformedResultObj)) {
                                            resultValue = transformedResultObj.getString(name);
                                        }
                                    }
                                    if (resultValue == null) {
                                        String rawResult = integrationResultVo.getRawResult();
                                        if (StringUtils.isNotEmpty(rawResult)) {
                                            config.put("result", rawResult);
                                            JSONObject rawResultObj = JSON.parseObject(rawResult);
                                            if (MapUtils.isNotEmpty(rawResultObj)) {
                                                resultValue = rawResultObj.getString(name);
                                            }
                                        }
                                    }
                                    if (resultValue != null) {
                                        List<String> curentValueList = new ArrayList<>();
                                        curentValueList.add(resultValue);
                                        String value = successConditionObj.getString("value");
                                        List<String> targetValueList = new ArrayList<>();
                                        targetValueList.add(value);
                                        String expression = successConditionObj.getString("expression");
                                        isSucceed = ConditionUtil.predicate(curentValueList, expression, targetValueList);
                                        if (!isSucceed) {
                                            String expressionName = Expression.getExpressionName(expression);
                                            failedReason = String.format("不满足成功条件：%s%s%s", name, expressionName, value);
                                        }
                                    }
                                }
                            } else {
                                String statusCode = String.valueOf(integrationResultVo.getStatusCode());
                                if (statusCode.startsWith("2") || statusCode.startsWith("3")) {
                                    isSucceed = true;
                                }
                            }
                        }

                        ProcessTaskActionVo actionVo = new ProcessTaskActionVo();
                        actionVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        actionVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                        actionVo.setProcessTaskStepName(currentProcessTaskStepVo.getName());
                        actionVo.setIntegrationUuid(integrationUuid);
                        actionVo.setIntegrationName(integrationVo.getName());
                        actionVo.setTrigger(triggerType.getTrigger());
                        actionVo.setTriggerText(triggerType.getText());
                        actionVo.setSucceed(isSucceed);
                        actionVo.setConfig(config);
                        if (isSucceed) {
                            actionVo.setStatus("succeed");
                        } else {
                            actionVo.setStatus("failed");
                            if (StringUtils.isNotBlank(integrationResultVo.getError())) {
                                String error = integrationResultVo.getError();
                                if (error.startsWith("failed\n")) {
                                    error = error.substring("failed\n".length());
                                }
                                actionVo.setError(error);
                            } else if (StringUtils.isNotBlank(failedReason)) {
                                actionVo.setError(failedReason);
                            }
                        }
                        processTaskActionMapper.insertProcessTaskAction(actionVo);
                        currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.RESTFULACTION.getParamName(), JSON.toJSONString(actionVo));
                        ProcessTaskAuditThread.audit(currentProcessTaskStepVo, ProcessTaskAuditType.RESTFULACTION);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("动作执行失败：" + ex.getMessage(), ex);
        }
    }

}
