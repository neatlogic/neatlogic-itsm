/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

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

package neatlogic.module.process.service;

import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationResultVo;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.constvalue.automatic.CallbackType;
import neatlogic.framework.process.constvalue.automatic.FailPolicy;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.automatic.AutomaticConfigVo;
import neatlogic.framework.process.dto.automatic.ProcessTaskStepAutomaticRequestVo;
import neatlogic.framework.process.handler.ProcessRequestFrom;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyTriggerType;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepHandlerUtil;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.ConditionUtil;
import neatlogic.framework.util.FreemarkerUtil;
import neatlogic.module.process.schedule.plugin.ProcessTaskAutomaticJob;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lvzk
 * @since 2021/8/16 15:47
 **/
@Service
public class ProcessTaskAutomaticServiceImpl implements ProcessTaskAutomaticService {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskAutomaticServiceImpl.class);
    @Resource
    private IntegrationMapper integrationMapper;
    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;
    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

//    @Override
//    public Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo) {
//        IntegrationResultVo resultVo = null;
//        boolean isUnloadJob = false;
//        ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(
//                new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(),
//                        ProcessTaskStepDataType.AUTOMATIC.getValue(), SystemUser.SYSTEM.getUserId()));
//        JSONObject data = auditDataVo.getData();
//        String integrationUuid = automaticConfigVo.getBaseIntegrationUuid();
//        JSONObject successConfig = automaticConfigVo.getBaseSuccessConfig();
//        String template = automaticConfigVo.getBaseResultTemplate();
//        JSONObject failConfig = null;
//        JSONObject audit = data.getJSONObject("requestAudit");
//        String resultJson;
//        if (!automaticConfigVo.getIsRequest()) {
//            audit = data.getJSONObject("callbackAudit");
//            template = automaticConfigVo.getCallbackResultTemplate();
//            integrationUuid = automaticConfigVo.getCallbackIntegrationUuid();
//            successConfig = automaticConfigVo.getCallbackSuccessConfig();
//            failConfig = automaticConfigVo.getCallbackFailConfig();
//        }
//        audit.put("startTime", System.currentTimeMillis());
//        JSONObject auditResult = new JSONObject();
//        audit.put("result", auditResult);
//        IProcessStepHandler processHandler =
//                ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
//        try {
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
//            audit.put("integrationName", integrationVo.getName());
//            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//            if (handler == null) {
//                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//            }
//            integrationVo.getParamObj().putAll(getIntegrationParam(automaticConfigVo, currentProcessTaskStepVo));
//            resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
//            resultJson = resultVo.getTransformedResult();
//            if (StringUtils.isBlank(resultVo.getTransformedResult())) {
//                resultJson = resultVo.getRawResult();
//            }
//            audit.put("endTime", System.currentTimeMillis());
//            auditResult.put("json", resultJson);
//            auditResult.put("template",
//                    FreemarkerUtil.transform(JSONObject.parse(resultVo.getTransformedResult()), template));
//           /* if (StringUtils.isNotBlank(resultVo.getError())) {
//                logger.error(resultVo.getError());
//                throw new MatrixExternalException("外部接口访问异常");
//            } else if (StringUtils.isNotBlank(resultJson)) {*/
//            if (predicate(successConfig, resultVo, true)) {// 如果执行成功
//                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.SUCCEED.getValue()));
//                if (!automaticConfigVo.getIsRequest() || !automaticConfigVo.getIsHasCallback()) {// 第一次请求
//                    //补充下一步骤id
//                    List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
//                    currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepIdList.get(0));
//                    processHandler.complete(currentProcessTaskStepVo);
//                } else {// 回调请求
//                    if (CallbackType.WAIT.getValue().equals(automaticConfigVo.getCallbackType())) {
//                        // 等待回调,挂起
//                        // processHandler.hang(currentProcessTaskStepVo);
//                    }
//                    if (CallbackType.INTERVAL.getValue().equals(automaticConfigVo.getCallbackType())) {
//                        automaticConfigVo.setIsRequest(false);
//                        automaticConfigVo.setResultJson(JSONObject.parseObject(resultJson));
//                        data =
//                                initProcessTaskStepData(currentProcessTaskStepVo, automaticConfigVo, data, "callback");
//                        initJob(automaticConfigVo, currentProcessTaskStepVo, data);
//                    }
//                }
//                isUnloadJob = true;
//            } else if (automaticConfigVo.getIsRequest()
//                    || (!automaticConfigVo.getIsRequest() && predicate(failConfig, resultVo, false))) {// 失败
//                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
//                //拼凑失败原因
//                String failedReason = StringUtils.EMPTY;
//                if (StringUtils.isNotBlank(resultVo.getError())) {
//                    failedReason = integrationVo.getUrl()+"\n"+resultVo.getError();
//                }else {
//                    if (MapUtils.isNotEmpty(successConfig)) {
//                        if (StringUtils.isBlank(successConfig.getString("name"))) {
//                            failedReason = "-";
//                        } else {
//                            failedReason = String.format("不满足成功条件：%s%s%s", successConfig.getString("name"), successConfig.getString("expressionName"), successConfig.getString("value"));
//                        }
//                    } else if (!automaticConfigVo.getIsRequest() && MapUtils.isNotEmpty(failConfig)) {
//                        if (StringUtils.isBlank(failConfig.getString("name"))) {
//                            failedReason = "-";
//                        } else {
//                            failedReason = String.format("满足失败条件：%s%s%s", failConfig.getString("name"), failConfig.getString("expressionName"), failConfig.getString("value"));
//                        }
//                    }
//                }
//                audit.put("failedReason", failedReason);
//                if (FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
//                    List<ProcessTaskStepVo> backStepList =
//                            processTaskService.getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo.getId());
//                    if (backStepList.size() == 1) {
//                        ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
//                        if (processHandler != null) {
//                            JSONObject jsonParam = currentProcessTaskStepVo.getParamObj();
//                            jsonParam.put("action", ProcessTaskOperationType.STEP_BACK.getValue());
//                            jsonParam.put("nextStepId", nextProcessTaskStepVo.getId());
//                            jsonParam.put("content", failedReason);
//                            processHandler.complete(currentProcessTaskStepVo);
//                        }
//                    } else {// 如果存在多个回退线，保持running
//                        // processHandler.hang(currentProcessTaskStepVo);
//                    }
//                } else if (FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
//                    //补充下一步骤id
//                    List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
//                    currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepIdList.get(0));
//                    processHandler.complete(currentProcessTaskStepVo);
//                } else if (FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
//                    processHandler.abortProcessTask(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
//                } else {// hang
//                    // processHandler.hang(currentProcessTaskStepVo);
//                }
//                isUnloadJob = true;
//            } else {
//                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.RUNNING.getValue()));
//                // continue
//            }
////            }
//
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//            audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
//            if (resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
//                audit.put("failedReason", resultVo.getError());
//            } else {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                ex.printStackTrace(pw);
//                audit.put("failedReason", sw.toString());
//            }
//            // processHandler.hang(currentProcessTaskStepVo);
//            isUnloadJob = true;
//        } finally {
//            auditDataVo.setData(data.toJSONString());
//            auditDataVo.setFcu(SystemUser.SYSTEM.getUserId());
//            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
//        }
//        return isUnloadJob;
//    }
//    /**
//     * 拼装入参数
//     *
//     * @param automaticConfigVo        自动化配置
//     * @param currentProcessTaskStepVo 当前步骤
//     * @return 参数对象
//     */
//    private JSONObject getIntegrationParam(AutomaticConfigVo automaticConfigVo,
//                                           ProcessTaskStepVo currentProcessTaskStepVo) {
//        ProcessTaskStepVo stepVo = processTaskService.getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
//        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
//        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
//        processTaskVo.setCurrentProcessTaskStep(stepVo);
//        JSONObject processTaskJson = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
//        JSONObject resultJson = automaticConfigVo.getResultJson();
//        JSONArray paramList = automaticConfigVo.getBaseParamList();
//        JSONObject integrationParam = new JSONObject();
//        if (!automaticConfigVo.getIsRequest()) {
//            paramList = automaticConfigVo.getCallbackParamList();
//        }
//        if (!CollectionUtils.isEmpty(paramList)) {
//            for (Object paramObj : paramList) {
//                JSONObject param = (JSONObject) paramObj;
//                String type = param.getString("type");
//                String value = param.getString("value");
//                String name = param.getString("name");
//                if (type.equals("common") || type.equals("form")) {
//                    integrationParam.put(name, processTaskJson.get(value.replaceAll("common#", StringUtils.EMPTY).replaceAll("form#", StringUtils.EMPTY)));
//                } else if (type.equals("integration")) {
//                    integrationParam.put(name, resultJson.get(value.replaceAll("integration#", StringUtils.EMPTY)));
//                } else {// 常量
//                    integrationParam.put(name, value);
//                }
//            }
//        }
//        return integrationParam;
//    }
//    @Override
//    public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo, AutomaticConfigVo automaticConfigVo, JSONObject data, String type) {
//        JSONObject failConfig = new JSONObject();
//        JSONObject successConfig = new JSONObject();
//        failConfig.put("default", "默认按状态码判断，4xx和5xx表示失败");
//        successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
//        // init request
//        if (type.equals("request")) {
//            data = new JSONObject();
//            JSONObject requestAudit = new JSONObject();
//            data.put("requestAudit", requestAudit);
//            requestAudit.put("integrationUuid", automaticConfigVo.getBaseIntegrationUuid());
//            requestAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
//            requestAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
//            requestAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
//            requestAudit.put("successConfig", automaticConfigVo.getBaseSuccessConfig());
//            if (automaticConfigVo.getBaseSuccessConfig() == null) {
//                requestAudit.put("successConfig", successConfig);
//            }
//            ProcessTaskStepDataVo auditDataVo =
//                    new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(),
//                            ProcessTaskStepDataType.AUTOMATIC.getValue(), SystemUser.SYSTEM.getUserId());
//            auditDataVo.setData(data.toJSONString());
//            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
//        } else {// init callback
//            JSONObject callbackAudit = new JSONObject();
//            callbackAudit.put("integrationUuid", automaticConfigVo.getCallbackIntegrationUuid());
//            callbackAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
//            callbackAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
//            callbackAudit.put("type", automaticConfigVo.getCallbackType());
//            callbackAudit.put("typeName", CallbackType.getText(automaticConfigVo.getCallbackType()));
//            callbackAudit.put("interval", automaticConfigVo.getCallbackInterval());
//            callbackAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
//            callbackAudit.put("successConfig", automaticConfigVo.getCallbackSuccessConfig());
//            if (automaticConfigVo.getCallbackFailConfig() == null) {
//                callbackAudit.put("failConfig", failConfig);
//            }
//            if (automaticConfigVo.getCallbackSuccessConfig() == null) {
//                callbackAudit.put("successConfig", successConfig);
//            }
//            data.put("callbackAudit", callbackAudit);
//        }
//        return data;
//    }
//    /**
//     * 初始化job
//     *
//     * @param automaticConfigVo 配置
//     * @param currentProcessTaskStepVo 当前步骤
//     */
//    @Override
//    public void initJob(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo,
//                        JSONObject data) {
//        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
//        if (jobHandler != null) {
//            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
//                    currentProcessTaskStepVo.getProcessTaskId().toString() + "-"
//                            + currentProcessTaskStepVo.getId().toString(),
//                    jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid())
//                    .addData("automaticConfigVo", automaticConfigVo).addData("data", data)
//                    .addData("currentProcessTaskStepVo", currentProcessTaskStepVo);
//            JobObject jobObject = jobObjectBuilder.build();
//            jobHandler.reloadJob(jobObject);
//        } else {
//            throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
//        }
//    }

    /**
     * 判断条件是否成立
     *
     * @param config    配置
     * @param resultVo  结果
     * @param isSuccess 是否成功
     * @return 是否成立
     */
    private Boolean predicate(JSONObject config, IntegrationResultVo resultVo, Boolean isSuccess) {
        boolean result = false;
        if (config == null || config.isEmpty() || !config.containsKey("expression")) {
            String patternStr = "(2|3).*";
            if (!isSuccess) {
                patternStr = "(4|5).*";
            }
            Pattern pattern = Pattern.compile(patternStr);
            if (pattern.matcher(String.valueOf(resultVo.getStatusCode())).matches()) {
                result = true;
            }
        } else {
            String name = config.getString("name");
            if (StringUtils.isNotBlank(name)) {
                String resultValue = null;
                String transformedResult = resultVo.getTransformedResult();
                if (StringUtils.isNotBlank(transformedResult)) {
                    Object value = JSONPath.read(transformedResult, name);
                    if (value != null) {
                        if (value instanceof String) {
                            resultValue = (String) value;
                        } else {
                            resultValue = value.toString();
                        }
                    }
//                    JSONObject transformedResultObj = JSON.parseObject(transformedResult);
//                    if (MapUtils.isNotEmpty(transformedResultObj)) {
//                        resultValue = transformedResultObj.getString(name);
//                    }
                }
                if (resultValue == null) {
                    String rawResult = resultVo.getRawResult();
                    if (StringUtils.isNotEmpty(rawResult)) {
                        Object value = JSONPath.read(transformedResult, name);
                        if (value != null) {
                            if (value instanceof String) {
                                resultValue = (String) value;
                            } else {
                                resultValue = value.toString();
                            }
                        }
//                        JSONObject rawResultObj = JSON.parseObject(rawResult);
//                        if (MapUtils.isNotEmpty(rawResultObj)) {
//                            resultValue = rawResultObj.getString(name);
//                        }
                    }
                }
                if (resultValue != null) {
                    List<String> currentValueList = new ArrayList<>();
                    currentValueList.add(resultValue);
                    String value = config.getString("value");
                    List<String> targetValueList = new ArrayList<>();
                    targetValueList.add(value);
                    String expression = config.getString("expression");
                    result = ConditionUtil.predicate(currentValueList, expression, targetValueList);
                }
            }
        }
        return result;
    }

    /**
     * 第一次请求
     *
     * @param currentProcessTaskStepVo
     */
    @Override
    @Transactional
    public void firstRequest(ProcessTaskStepVo currentProcessTaskStepVo) {
//        System.out.println("firstRequest start");
        AutomaticConfigVo automaticConfigVo = getAutomaticConfigVoByProcessTaskStepId(currentProcessTaskStepVo.getId());
        String integrationUuid = automaticConfigVo.getBaseIntegrationUuid();
        JSONObject successConfig = automaticConfigVo.getBaseSuccessConfig();
        String template = automaticConfigVo.getBaseResultTemplate();
        ProcessTaskStepDataVo searchVo = new ProcessTaskStepDataVo(
                currentProcessTaskStepVo.getProcessTaskId(),
                currentProcessTaskStepVo.getId(),
                ProcessTaskStepDataType.AUTOMATIC.getValue(),
                SystemUser.SYSTEM.getUserUuid()
        );
        ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(searchVo);
        JSONObject data = auditDataVo.getData();
        JSONObject requestAudit = data.getJSONObject("requestAudit");
//        requestAudit.put("startTime", System.currentTimeMillis());
        IntegrationResultVo resultVo = null;
        try {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
            requestAudit.put("integrationName", integrationVo.getName());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }
            integrationVo.getParamObj().putAll(getIntegrationParam(currentProcessTaskStepVo, automaticConfigVo.getBaseParamList(), null));
            resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
//            System.out.println("resultVo=" + JSONObject.toJSONString(resultVo));
            String resultJson = resultVo.getTransformedResult();
            if (StringUtils.isBlank(resultJson)) {
                resultJson = resultVo.getRawResult();
            }
            requestAudit.put("endTime", System.currentTimeMillis());
            JSONObject auditResult = new JSONObject();
            auditResult.put("json", resultJson);
            auditResult.put("template", FreemarkerUtil.transform(JSONObject.parse(resultJson), template));
            requestAudit.put("result", auditResult);

            if (predicate(successConfig, resultVo, true)) {// 如果执行成功
//                System.out.println("firstRequest 成功");
                requestAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.SUCCEED.getValue()));
                if (automaticConfigVo.getIsHasCallback()) {// 第一次请求
//                    System.out.println("需要回调");
                    // 回调请求
                    if (CallbackType.INTERVAL.getValue().equals(automaticConfigVo.getCallbackType())) {
                        JSONObject callbackAudit = getCallbackAudit(automaticConfigVo);
                        data.put("callbackAudit", callbackAudit);
                        auditDataVo.setData(data.toJSONString());
                        auditDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
                        processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
                        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
                        if (jobHandler == null) {
                            throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
                        }
                        ProcessTaskStepAutomaticRequestVo processTaskStepAutomaticRequestVo = new ProcessTaskStepAutomaticRequestVo();
                        processTaskStepAutomaticRequestVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        processTaskStepAutomaticRequestVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                        processTaskStepAutomaticRequestVo.setType("callback");
                        processTaskMapper.insertProcessTaskStepAutomaticRequest(processTaskStepAutomaticRequestVo);
                        JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                                processTaskStepAutomaticRequestVo.getId().toString(),
                                jobHandler.getGroupName(),
                                jobHandler.getClassName(),
                                TenantContext.get().getTenantUuid()
                        );
                        JobObject jobObject = jobObjectBuilder.build();
                        jobHandler.reloadJob(jobObject);
                    }
                } else { //流转到下一步
//                    System.out.println("不需要回调");
                    auditDataVo.setData(data.toJSONString());
                    auditDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
                    processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
                    //补充下一步骤id
                    List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
//                    currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepIdList.get(0));
                    JSONObject jsonParam = currentProcessTaskStepVo.getParamObj();
                    jsonParam.put("action", ProcessTaskOperationType.STEP_COMPLETE.getValue());
                    jsonParam.put("nextStepId", nextStepIdList.get(0));
                    jsonParam.put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), data);
                    IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                    processHandler.autoComplete(currentProcessTaskStepVo);
                }
            } else {// 失败
//                System.out.println("firstRequest 失败");
                requestAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.FAILED.getValue()));
                //拼凑失败原因
                String failedReason = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    failedReason = integrationVo.getUrl() + "\n" + resultVo.getError();
                } else {
                    if (MapUtils.isNotEmpty(successConfig)) {
                        if (StringUtils.isBlank(successConfig.getString("name"))) {
                            failedReason = "-";
                        } else {
                            failedReason = String.format("不满足成功条件：%s%s%s", successConfig.getString("name"), successConfig.getString("expressionName"), successConfig.getString("value"));
                        }
                    }
                }
                requestAudit.put("failedReason", failedReason);
                failPolicy(automaticConfigVo, currentProcessTaskStepVo, failedReason, data);
                auditDataVo.setData(data.toJSONString());
                auditDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
                processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
                currentProcessTaskStepVo.getParamObj().put("actionFailedContent", failedReason);
                processStepHandlerUtil.notify(currentProcessTaskStepVo, ProcessTaskStepAutomaticNotifyTriggerType.ACTION_FAILED);
            }

        } catch (Exception ex) {
//            System.out.println("firstRequest 异常");
            logger.error(ex.getMessage(), ex);
            requestAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.FAILED.getValue()));
            if (resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
                requestAudit.put("failedReason", resultVo.getError());
                currentProcessTaskStepVo.getParamObj().put("actionFailedContent", resultVo.getError());
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                requestAudit.put("failedReason", sw.toString());
                currentProcessTaskStepVo.getParamObj().put("actionFailedContent", sw.toString());
            }
            auditDataVo.setData(data.toJSONString());
            auditDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
            processStepHandlerUtil.notify(currentProcessTaskStepVo, ProcessTaskStepAutomaticNotifyTriggerType.ACTION_FAILED);
        }
//        System.out.println("firstRequest end");
    }

    /**
     * 回调请求
     *
     * @param currentProcessTaskStepVo
     * @return
     */
    @Override
    @Transactional
    public boolean callbackRequest(ProcessTaskStepVo currentProcessTaskStepVo) {
//        System.out.println("callbackRequest start");
        AutomaticConfigVo automaticConfigVo = getAutomaticConfigVoByProcessTaskStepId(currentProcessTaskStepVo.getId());
        String template = automaticConfigVo.getCallbackResultTemplate();
        String integrationUuid = automaticConfigVo.getCallbackIntegrationUuid();
        JSONObject successConfig = automaticConfigVo.getCallbackSuccessConfig();
        JSONObject failConfig = automaticConfigVo.getCallbackFailConfig();
        boolean isUnloadJob = false;
        ProcessTaskStepDataVo searchVo = new ProcessTaskStepDataVo(
                currentProcessTaskStepVo.getProcessTaskId(),
                currentProcessTaskStepVo.getId(),
                ProcessTaskStepDataType.AUTOMATIC.getValue(),
                SystemUser.SYSTEM.getUserUuid()
        );
        ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(searchVo);
        JSONObject data = auditDataVo.getData();
        JSONObject callbackAudit = data.getJSONObject("callbackAudit");
        callbackAudit.put("startTime", System.currentTimeMillis());
        IntegrationResultVo resultVo = null;
        try {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
            callbackAudit.put("integrationName", integrationVo.getName());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }
            String json = null;
            JSONObject requestAudit = data.getJSONObject("requestAudit");
            if (MapUtils.isNotEmpty(requestAudit)) {
                JSONObject result = requestAudit.getJSONObject("result");
                if (MapUtils.isNotEmpty(result)) {
                    json = result.getString("json");
                }
            }
            integrationVo.getParamObj().putAll(getIntegrationParam(currentProcessTaskStepVo, automaticConfigVo.getCallbackParamList(), JSONObject.parseObject(json)));
            resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
//            System.out.println("resultVo=" + JSONObject.toJSONString(resultVo));
            String resultJson = resultVo.getTransformedResult();
            if (StringUtils.isBlank(resultJson)) {
                resultJson = resultVo.getRawResult();
            }
            callbackAudit.put("endTime", System.currentTimeMillis());
            JSONObject auditResult = new JSONObject();
            auditResult.put("json", resultJson);
            auditResult.put("template", FreemarkerUtil.transform(JSONObject.parse(resultJson), template));
            callbackAudit.put("result", auditResult);

            if (predicate(successConfig, resultVo, true)) {// 如果执行成功
//                System.out.println("callbackRequest 成功");
                callbackAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.SUCCEED.getValue()));
                //补充下一步骤id
                List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
//                currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepIdList.get(0));
                JSONObject jsonParam = currentProcessTaskStepVo.getParamObj();
                jsonParam.put("action", ProcessTaskOperationType.STEP_COMPLETE.getValue());
                jsonParam.put("nextStepId", nextStepIdList.get(0));
                jsonParam.put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), data);
                IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                processHandler.autoComplete(currentProcessTaskStepVo);
                isUnloadJob = true;
            } else if (predicate(failConfig, resultVo, false)) {// 失败
//                System.out.println("callbackRequest 失败");
                callbackAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.FAILED.getValue()));
                //拼凑失败原因
                String failedReason = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    failedReason = integrationVo.getUrl() + "\n" + resultVo.getError();
                } else {
                    if (MapUtils.isNotEmpty(failConfig)) {
                        if (StringUtils.isBlank(failConfig.getString("name"))) {
                            failedReason = "-";
                        } else {
                            failedReason = String.format("满足失败条件：%s%s%s", failConfig.getString("name"), failConfig.getString("expressionName"), failConfig.getString("value"));
                        }
                    }
                }
                callbackAudit.put("failedReason", failedReason);
                failPolicy(automaticConfigVo, currentProcessTaskStepVo, failedReason, data);
                isUnloadJob = true;
            } else {
//                System.out.println("callbackRequest 继续轮询回调");
                callbackAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.RUNNING.getValue()));
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            callbackAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.FAILED.getValue()));
            if (resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
                callbackAudit.put("failedReason", resultVo.getError());
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                callbackAudit.put("failedReason", sw.toString());
            }
            isUnloadJob = true;
        } finally {
            auditDataVo.setData(data.toJSONString());
            auditDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
        }
//        System.out.println("callbackRequest end");
        return isUnloadJob;
    }

    /**
     * 获取流程步骤自动处理配置信息
     *
     * @param processTaskStepId
     * @return
     */
    @Override
    public AutomaticConfigVo getAutomaticConfigVoByProcessTaskStepId(Long processTaskStepId) {
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        //获取参数
        JSONObject automaticConfig = (JSONObject) JSONPath.read(stepConfig, "automaticConfig");
        return new AutomaticConfigVo(automaticConfig);
    }

    /**
     * 组装请求参数
     *
     * @param currentProcessTaskStepVo
     * @param paramList
     * @param resultJson
     * @return
     */
    private JSONObject getIntegrationParam(ProcessTaskStepVo currentProcessTaskStepVo, JSONArray paramList, JSONObject resultJson) {
        JSONObject processTaskJson = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), currentProcessTaskStepVo);
//        ProcessTaskStepVo stepVo = processTaskService.getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
//        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
//        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
//        processTaskVo.setCurrentProcessTaskStep(stepVo);
//        JSONObject processTaskJson = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
        JSONObject integrationParam = new JSONObject();
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (Object paramObj : paramList) {
                JSONObject param = (JSONObject) paramObj;
                String type = param.getString("type");
                String value = param.getString("value");
                String name = param.getString("name");
                if (type.equals("common") || type.equals("form")) {
                    integrationParam.put(name, processTaskJson.get(value.replaceAll("common#", StringUtils.EMPTY).replaceAll("form#", StringUtils.EMPTY)));
                } else if (type.equals("integration") && MapUtils.isNotEmpty(resultJson)) {
                    integrationParam.put(name, resultJson.get(value.replaceAll("integration#", StringUtils.EMPTY)));
                } else {// 常量
                    integrationParam.put(name, value);
                }
            }
        }
        return integrationParam;
    }

    /**
     * 获取回调信息
     *
     * @param automaticConfigVo
     * @return
     */
    private JSONObject getCallbackAudit(AutomaticConfigVo automaticConfigVo) {
        JSONObject failConfig = new JSONObject();
        JSONObject successConfig = new JSONObject();
        failConfig.put("default", "默认按状态码判断，4xx和5xx表示失败");
        successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
        JSONObject callbackAudit = new JSONObject();
        callbackAudit.put("integrationUuid", automaticConfigVo.getCallbackIntegrationUuid());
        callbackAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
        callbackAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
        callbackAudit.put("type", automaticConfigVo.getCallbackType());
        callbackAudit.put("typeName", CallbackType.getText(automaticConfigVo.getCallbackType()));
        callbackAudit.put("interval", automaticConfigVo.getCallbackInterval());
        callbackAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.PENDING.getValue()));
        callbackAudit.put("successConfig", automaticConfigVo.getCallbackSuccessConfig());
        if (automaticConfigVo.getCallbackFailConfig() == null) {
            callbackAudit.put("failConfig", failConfig);
        }
        if (automaticConfigVo.getCallbackSuccessConfig() == null) {
            callbackAudit.put("successConfig", successConfig);
        }
        return callbackAudit;
    }

    /**
     * 失败策略
     *
     * @param automaticConfigVo
     * @param currentProcessTaskStepVo
     * @param failedReason
     */
    private void failPolicy(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo, String failedReason, JSONObject automaticInfo) {
        IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
            List<ProcessTaskStepVo> backStepList =
                    processTaskService.getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo.getId());
            if (backStepList.size() == 1) {
                ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
                if (processHandler != null) {
                    JSONObject jsonParam = currentProcessTaskStepVo.getParamObj();
                    jsonParam.put("action", ProcessTaskOperationType.STEP_BACK.getValue());
                    jsonParam.put("nextStepId", nextProcessTaskStepVo.getId());
                    jsonParam.put("content", failedReason);
                    jsonParam.put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), automaticInfo);
                    processHandler.autoComplete(currentProcessTaskStepVo);
                }
            }
        } else if (FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
            //补充下一步骤id
            List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
//            currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepIdList.get(0));
            JSONObject jsonParam = currentProcessTaskStepVo.getParamObj();
            jsonParam.put("action", ProcessTaskOperationType.STEP_COMPLETE.getValue());
            jsonParam.put("nextStepId", nextStepIdList.get(0));
            jsonParam.put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), automaticInfo);
            processHandler.autoComplete(currentProcessTaskStepVo);
        } else if (FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
            ProcessTaskVo processTaskVo = new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId());
            processTaskVo.getParamObj().put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), automaticInfo);
            processHandler.abortProcessTask(processTaskVo);
        }
    }
}
