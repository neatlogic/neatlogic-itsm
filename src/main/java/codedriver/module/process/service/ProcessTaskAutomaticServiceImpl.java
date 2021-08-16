/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.constvalue.automatic.CallbackType;
import codedriver.framework.process.constvalue.automatic.FailPolicy;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.handler.ProcessRequestFrom;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.util.ConditionUtil;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.module.process.schedule.plugin.ProcessTaskAutomaticJob;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author lvzk
 * @since 2021/8/16 15:47
 **/
@Service
public class ProcessTaskAutomaticServiceImpl implements ProcessTaskAutomaticService{
    static Logger logger = LoggerFactory.getLogger(ProcessTaskAutomaticServiceImpl.class);
    @Resource
    private IntegrationMapper integrationMapper;
    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;
    @Resource
    ProcessTaskMapper processTaskMapper;

    @Resource
    ProcessTaskService processTaskService;

    @Override
    public Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo) {
        IntegrationResultVo resultVo = null;
        boolean isUnloadJob = false;
        ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(
                new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(),
                        ProcessTaskStepDataType.AUTOMATIC.getValue(), SystemUser.SYSTEM.getUserId()));
        JSONObject data = auditDataVo.getData();
        String integrationUuid = automaticConfigVo.getBaseIntegrationUuid();
        JSONObject successConfig = automaticConfigVo.getBaseSuccessConfig();
        String template = automaticConfigVo.getBaseResultTemplate();
        JSONObject failConfig = null;
        JSONObject audit = data.getJSONObject("requestAudit");
        String resultJson;
        if (!automaticConfigVo.getIsRequest()) {
            audit = data.getJSONObject("callbackAudit");
            template = automaticConfigVo.getCallbackResultTemplate();
            integrationUuid = automaticConfigVo.getCallbackIntegrationUuid();
            successConfig = automaticConfigVo.getCallbackSuccessConfig();
            failConfig = automaticConfigVo.getCallbackFailConfig();
        }
        audit.put("startTime", System.currentTimeMillis());
        JSONObject auditResult = new JSONObject();
        audit.put("result", auditResult);
        IProcessStepHandler processHandler =
                ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        try {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
            audit.put("integrationName", integrationVo.getName());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }
            integrationVo.getParamObj().putAll(getIntegrationParam(automaticConfigVo, currentProcessTaskStepVo));
            resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
            resultJson = resultVo.getTransformedResult();
            if (StringUtils.isBlank(resultVo.getTransformedResult())) {
                resultJson = resultVo.getRawResult();
            }
            audit.put("endTime", System.currentTimeMillis());
            auditResult.put("json", resultJson);
            auditResult.put("template",
                    FreemarkerUtil.transform(JSONObject.parse(resultVo.getTransformedResult()), template));
           /* if (StringUtils.isNotBlank(resultVo.getError())) {
                logger.error(resultVo.getError());
                throw new MatrixExternalException("外部接口访问异常");
            } else if (StringUtils.isNotBlank(resultJson)) {*/
            if (predicate(successConfig, resultVo, true)) {// 如果执行成功
                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.SUCCEED.getValue()));
                if (!automaticConfigVo.getIsRequest() || !automaticConfigVo.getIsHasCallback()) {// 第一次请求
                    //补充下一步骤id
                    List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
                    currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepList.get(0).getId());
                    processHandler.complete(currentProcessTaskStepVo);
                } else {// 回调请求
                    if (CallbackType.WAIT.getValue().equals(automaticConfigVo.getCallbackType())) {
                        // 等待回调,挂起
                        // processHandler.hang(currentProcessTaskStepVo);
                    }
                    if (CallbackType.INTERVAL.getValue().equals(automaticConfigVo.getCallbackType())) {
                        automaticConfigVo.setIsRequest(false);
                        automaticConfigVo.setResultJson(JSONObject.parseObject(resultJson));
                        data =
                                initProcessTaskStepData(currentProcessTaskStepVo, automaticConfigVo, data, "callback");
                        initJob(automaticConfigVo, currentProcessTaskStepVo, data);
                    }
                }
                isUnloadJob = true;
            } else if (automaticConfigVo.getIsRequest()
                    || (!automaticConfigVo.getIsRequest() && predicate(failConfig, resultVo, false))) {// 失败
                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
                //拼凑失败原因
                String failedReason = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    failedReason = integrationVo.getUrl()+"\n"+resultVo.getError();
                }else {
                    if (MapUtils.isNotEmpty(successConfig)) {
                        if (StringUtils.isBlank(successConfig.getString("name"))) {
                            failedReason = "-";
                        } else {
                            failedReason = String.format("不满足成功条件：%s%s%s", successConfig.getString("name"), successConfig.getString("expressionName"), successConfig.getString("value"));
                        }
                    } else if (!automaticConfigVo.getIsRequest() && MapUtils.isNotEmpty(failConfig)) {
                        if (StringUtils.isBlank(failConfig.getString("name"))) {
                            failedReason = "-";
                        } else {
                            failedReason = String.format("满足失败条件：%s%s%s", failConfig.getString("name"), failConfig.getString("expressionName"), failConfig.getString("value"));
                        }
                    }
                }
                audit.put("failedReason", failedReason);
                if (FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
                    List<ProcessTaskStepVo> backStepList =
                            processTaskService.getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo.getId());
                    if (backStepList.size() == 1) {
                        ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
                        if (processHandler != null) {
                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("action", ProcessTaskOperationType.STEP_BACK.getValue());
                            jsonParam.put("nextStepId", nextProcessTaskStepVo.getId());
                            jsonParam.put("content", failedReason);
                            currentProcessTaskStepVo.setParamObj(jsonParam);
                            processHandler.complete(currentProcessTaskStepVo);
                        }
                    } else {// 如果存在多个回退线，保持running
                        // processHandler.hang(currentProcessTaskStepVo);
                    }
                } else if (FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
                    //补充下一步骤id
                    List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
                    currentProcessTaskStepVo.getParamObj().put("nextStepId", nextStepList.get(0).getId());
                    processHandler.complete(currentProcessTaskStepVo);
                } else if (FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
                    processHandler.abortProcessTask(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
                } else {// hang
                    // processHandler.hang(currentProcessTaskStepVo);
                }
                isUnloadJob = true;
            } else {
                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.RUNNING.getValue()));
                // continue
            }
//            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
            if (resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
                audit.put("failedReason", resultVo.getError());
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                audit.put("failedReason", sw.toString());
            }
            // processHandler.hang(currentProcessTaskStepVo);
            isUnloadJob = true;
        } finally {
            auditDataVo.setData(data.toJSONString());
            auditDataVo.setFcu(SystemUser.SYSTEM.getUserId());
            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
        }
        return isUnloadJob;
    }
    /**
     * 拼装入参数
     *
     * @param automaticConfigVo        自动化配置
     * @param currentProcessTaskStepVo 当前步骤
     * @return 参数对象
     */
    private JSONObject getIntegrationParam(AutomaticConfigVo automaticConfigVo,
                                           ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepVo stepVo = processTaskService.getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
        processTaskVo.setCurrentProcessTaskStep(stepVo);
        JSONObject processTaskJson = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
        JSONObject resultJson = automaticConfigVo.getResultJson();
        JSONArray paramList = automaticConfigVo.getBaseParamList();
        JSONObject integrationParam = new JSONObject();
        if (!automaticConfigVo.getIsRequest()) {
            paramList = automaticConfigVo.getCallbackParamList();
        }
        if (!CollectionUtils.isEmpty(paramList)) {
            for (Object paramObj : paramList) {
                JSONObject param = (JSONObject) paramObj;
                String type = param.getString("type");
                String value = param.getString("value");
                String name = param.getString("name");
                if (type.equals("common") || type.equals("form")) {
                    integrationParam.put(name, processTaskJson.get(value.replaceAll("common#", StringUtils.EMPTY).replaceAll("form#", StringUtils.EMPTY)));
                } else if (type.equals("integration")) {
                    integrationParam.put(name, resultJson.get(value.replaceAll("integration#", StringUtils.EMPTY)));
                } else {// 常量
                    integrationParam.put(name, value);
                }
            }
        }
        return integrationParam;
    }
    @Override
    public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo, AutomaticConfigVo automaticConfigVo, JSONObject data, String type) {
        JSONObject failConfig = new JSONObject();
        JSONObject successConfig = new JSONObject();
        failConfig.put("default", "默认按状态码判断，4xx和5xx表示失败");
        successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
        // init request
        if (type.equals("request")) {
            data = new JSONObject();
            JSONObject requestAudit = new JSONObject();
            data.put("requestAudit", requestAudit);
            requestAudit.put("integrationUuid", automaticConfigVo.getBaseIntegrationUuid());
            requestAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
            requestAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
            requestAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
            requestAudit.put("successConfig", automaticConfigVo.getBaseSuccessConfig());
            if (automaticConfigVo.getBaseSuccessConfig() == null) {
                requestAudit.put("successConfig", successConfig);
            }
            ProcessTaskStepDataVo auditDataVo =
                    new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(),
                            ProcessTaskStepDataType.AUTOMATIC.getValue(), SystemUser.SYSTEM.getUserId());
            auditDataVo.setData(data.toJSONString());
            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
        } else {// init callback
            JSONObject callbackAudit = new JSONObject();
            callbackAudit.put("integrationUuid", automaticConfigVo.getCallbackIntegrationUuid());
            callbackAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
            callbackAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
            callbackAudit.put("type", automaticConfigVo.getCallbackType());
            callbackAudit.put("typeName", CallbackType.getText(automaticConfigVo.getCallbackType()));
            callbackAudit.put("interval", automaticConfigVo.getCallbackInterval());
            callbackAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
            callbackAudit.put("successConfig", automaticConfigVo.getCallbackSuccessConfig());
            if (automaticConfigVo.getCallbackFailConfig() == null) {
                callbackAudit.put("failConfig", failConfig);
            }
            if (automaticConfigVo.getCallbackSuccessConfig() == null) {
                callbackAudit.put("successConfig", successConfig);
            }
            data.put("callbackAudit", callbackAudit);
        }
        return data;
    }
    /**
     * 初始化job
     *
     * @param automaticConfigVo 配置
     * @param currentProcessTaskStepVo 当前步骤
     */
    @Override
    public void initJob(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo,
                        JSONObject data) {
        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
        if (jobHandler != null) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    currentProcessTaskStepVo.getProcessTaskId().toString() + "-"
                            + currentProcessTaskStepVo.getId().toString(),
                    jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid())
                    .addData("automaticConfigVo", automaticConfigVo).addData("data", data)
                    .addData("currentProcessTaskStepVo", currentProcessTaskStepVo);
            JobObject jobObject = jobObjectBuilder.build();
            jobHandler.reloadJob(jobObject);
        } else {
            throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
        }
    }

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
                    JSONObject transformedResultObj = JSON.parseObject(transformedResult);
                    if (MapUtils.isNotEmpty(transformedResultObj)) {
                        resultValue = transformedResultObj.getString(name);
                    }
                }
                if (resultValue == null) {
                    String rawResult = resultVo.getRawResult();
                    if (StringUtils.isNotEmpty(rawResult)) {
                        JSONObject rawResultObj = JSON.parseObject(rawResult);
                        if (MapUtils.isNotEmpty(rawResultObj)) {
                            resultValue = rawResultObj.getString(name);
                        }
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
}
