package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.notify.core.INotifyTriggerType;
import codedriver.framework.notify.dto.ParamMappingVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ActionVo;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.module.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.process.notify.constvalue.TaskNotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.util.ConditionUtil;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: ActionHandler
 * @Package codedriver.module.process.thread
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 17:13
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class ProcessTaskActionThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static ProcessStepHandlerMapper processStepHandlerMapper;
    private static IntegrationMapper integrationMapper;
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
    public void setProcessStepHandlerMapper(ProcessStepHandlerMapper _processStepHandlerMapper) {
        processStepHandlerMapper = _processStepHandlerMapper;
    }

    @Autowired
    public void setIntegrationMapper(IntegrationMapper _integrationMapper) {
        integrationMapper = _integrationMapper;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private INotifyTriggerType triggerType;

    public ProcessTaskActionThread(){}
    public ProcessTaskActionThread(ProcessTaskStepVo _currentProcessTaskStepVo, INotifyTriggerType _trigger) {
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        triggerType = _trigger;
        if (_currentProcessTaskStepVo != null) {
            this.setThreadName("PROCESSTASK-ACTION-" + _currentProcessTaskStepVo.getId());
        }
    }

    @Override
    protected void execute() {
        try {
            JSONArray actionList = null;
            if (triggerType instanceof TaskNotifyTriggerType) {
                /** 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                actionList = (JSONArray) JSONPath.read(config, "process.processConfig.actionConfig.actionList");
            } else {
                /** 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                actionList = (JSONArray)JSONPath.read(stepConfig, "actionConfig.actionList");
                if (CollectionUtils.isEmpty(actionList)) {
                    IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(stepVo.getHandler());
                    if (processStepUtilHandler == null) {
                        throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                    }
                    ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(stepVo.getHandler());
                    JSONObject globalConfig = processStepUtilHandler.makeupConfig(processStepHandlerVo != null ? processStepHandlerVo.getConfig() : null);
                    actionList = (JSONArray)JSONPath.read(JSON.toJSONString(globalConfig), "actionConfig.actionList");
                }
            }

            /** 从步骤配置信息中获取动作列表 **/
            if (CollectionUtils.isNotEmpty(actionList)) {
                for (int i = 0; i < actionList.size(); i++) {
                    JSONObject actionObj = actionList.getJSONObject(i);
                    if (triggerType.getTrigger().equals(actionObj.getString("trigger"))) {
                        String integrationUuid = actionObj.getString("integrationUuid");
                        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
                        if (integrationVo == null) {
                            throw new IntegrationNotFoundException(integrationUuid);
                        }
                        IIntegrationHandler iIntegrationHandler =
                                IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
                        if (iIntegrationHandler == null) {
                            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
                        }
                        /** 参数映射 **/
                        List<ParamMappingVo> paramMappingList = JSON.parseArray(
                                actionObj.getJSONArray("paramMappingList").toJSONString(), ParamMappingVo.class);
                        if (CollectionUtils.isNotEmpty(paramMappingList)) {
                            ProcessTaskVo processTaskVo =
                                    processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
                            processTaskVo.setStartProcessTaskStep(
                                    processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
                            processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
                            JSONObject processFieldData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
                            for (ParamMappingVo paramMappingVo : paramMappingList) {
                                if (ProcessFieldType.CONSTANT.getValue().equals(paramMappingVo.getType())) {
                                    integrationVo.getParamObj().put(paramMappingVo.getName(),
                                            paramMappingVo.getValue());
                                } else if (StringUtils.isNotBlank(paramMappingVo.getType())) {
                                    Object processFieldValue = processFieldData.get(paramMappingVo.getValue());
                                    if (processFieldValue != null) {
                                        integrationVo.getParamObj().put(paramMappingVo.getName(),
                                                processFieldValue);
                                    } else {
                                        logger.error("没有找到参数'" + paramMappingVo.getValue() + "'信息");
                                    }
                                }
                            }
                        }
                        boolean isSucceed = false;
                        IntegrationResultVo integrationResultVo =
                                iIntegrationHandler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
                        if (StringUtils.isNotBlank(integrationResultVo.getError())) {
                            logger.error(integrationResultVo.getError());
//                                throw new IntegrationSendRequestException(integrationVo.getUuid());
                        }else {
                            JSONObject successConditionObj = actionObj.getJSONObject("successCondition");
                            if (MapUtils.isNotEmpty(successConditionObj)) {
                                String name = successConditionObj.getString("name");
                                if (StringUtils.isNotBlank(name)) {
                                    String resultValue = null;
                                    String transformedResult = integrationResultVo.getTransformedResult();
                                    if (StringUtils.isNotBlank(transformedResult)) {
                                        JSONObject transformedResultObj = JSON.parseObject(transformedResult);
                                        if (MapUtils.isNotEmpty(transformedResultObj)) {
                                            resultValue = transformedResultObj.getString(name);
                                        }
                                    }
                                    if (resultValue == null) {
                                        String rawResult = integrationResultVo.getRawResult();
                                        if (StringUtils.isNotEmpty(rawResult)) {
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
                                        isSucceed =
                                                ConditionUtil.predicate(curentValueList, expression, targetValueList);
                                    }
                                }
                            } else {
                                String statusCode = String.valueOf(integrationResultVo.getStatusCode());
                                if (statusCode.startsWith("2") || statusCode.startsWith("3")) {
                                    isSucceed = true;
                                }
                            }
                        }

                        ActionVo actionVo = new ActionVo();
                        actionVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                        actionVo.setProcessTaskStepName(currentProcessTaskStepVo.getName());
                        actionVo.setIntegrationUuid(integrationUuid);
                        actionVo.setIntegrationName(integrationVo.getName());
                        actionVo.setTrigger(triggerType.getTrigger());
                        actionVo.setTriggerText(triggerType.getText());
                        actionVo.setSucceed(isSucceed);
                        JSONObject paramObj = new JSONObject();
                        paramObj.put(ProcessTaskAuditDetailType.RESTFULACTION.getParamName(),
                                JSON.toJSONString(actionVo));
                        currentProcessTaskStepVo.setParamObj(paramObj);
                        ProcessTaskAuditThread.audit(currentProcessTaskStepVo, ProcessTaskAuditType.RESTFULACTION);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("动作执行失败：" + ex.getMessage(), ex);
        }
    }

}
