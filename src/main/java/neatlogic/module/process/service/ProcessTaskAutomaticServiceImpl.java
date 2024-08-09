/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
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
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.automatic.AutomaticConfigVo;
import neatlogic.framework.process.dto.automatic.ProcessTaskStepAutomaticRequestVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.handler.ProcessRequestFrom;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyTriggerType;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.ConditionUtil;
import neatlogic.framework.util.FreemarkerUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.schedule.plugin.ProcessTaskAutomaticJob;
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
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskAutomaticServiceImpl.class);
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
            String patternStr = "([23]).*";
            if (!isSuccess) {
                patternStr = "([45]).*";
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
                List<String> currentValueList = new ArrayList<>();
                if (StringUtils.isNotBlank(resultValue)) {
                    currentValueList.add(resultValue);
                }
                List<String> targetValueList = new ArrayList<>();
                String value = config.getString("value");
                if (StringUtils.isNotBlank(value)) {
                    targetValueList.add(value);
                }
                String expression = config.getString("expression");
                try {
                    result = ConditionUtil.predicate(currentValueList, expression, targetValueList);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * 第一次请求
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
            String templateResult = FreemarkerUtil.transform(JSON.parse(resultJson), template);
            auditResult.put("template", templateResult);
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
                        String name = successConfig.getString("name");
                        if (StringUtils.isBlank(name)) {
                            failedReason = "-";
                        } else {
                            String expressionName = successConfig.getString("expressionName");
                            if (expressionName == null) {
                                expressionName = StringUtils.EMPTY;
                            }
                            String value = successConfig.getString("value");
                            if (value == null) {
                                value = StringUtils.EMPTY;
                            }
                            failedReason = String.format("不满足成功条件：%s%s%s", name, expressionName, value);
                        }
                    }
                }
                if (StringUtils.isNotBlank(templateResult)) {
                    failedReason = failedReason + "\n" + templateResult;
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
            integrationVo.getParamObj().putAll(getIntegrationParam(currentProcessTaskStepVo, automaticConfigVo.getCallbackParamList(), JSON.parseObject(json)));
            resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.PROCESS);
//            System.out.println("resultVo=" + JSONObject.toJSONString(resultVo));
            String resultJson = resultVo.getTransformedResult();
            if (StringUtils.isBlank(resultJson)) {
                resultJson = resultVo.getRawResult();
            }
            callbackAudit.put("endTime", System.currentTimeMillis());
            JSONObject auditResult = new JSONObject();
            auditResult.put("json", resultJson);
            String templateResult = FreemarkerUtil.transform(JSON.parse(resultJson), template);
            auditResult.put("template", templateResult);
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
                        String name = failConfig.getString("name");
                        if (StringUtils.isBlank(name)) {
                            failedReason = "-";
                        } else {
                            String expressionName = failConfig.getString("expressionName");
                            if (expressionName == null) {
                                expressionName = StringUtils.EMPTY;
                            }
                            String value = failConfig.getString("value");
                            if (value == null) {
                                value = StringUtils.EMPTY;
                            }
                            failedReason = String.format("满足失败条件：%s%s%s", name, expressionName, value);
                        }
                    }
                }
                if (StringUtils.isNotBlank(templateResult)) {
                    failedReason = failedReason + "\n" + templateResult;
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
     */
    private JSONObject getIntegrationParam(ProcessTaskStepVo currentProcessTaskStepVo, JSONArray paramList, JSONObject resultJson) {
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        String formTag = null;
        if (StringUtils.isNotBlank(stepConfig)) {
            formTag = (String) JSONPath.read(stepConfig, "formTag");
        }
        List<String> processTaskParams = Arrays.stream(ProcessTaskParams.values()).map(ProcessTaskParams::getValue).collect(Collectors.toList());
        JSONObject processTaskJson = ProcessTaskConditionFactory.getConditionParamData(processTaskParams, currentProcessTaskStepVo, formTag);
        JSONObject integrationParam = new JSONObject();
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (Object paramObj : paramList) {
                JSONObject param = (JSONObject) paramObj;
                String type = param.getString("type");
                String value = param.getString("value");
                String name = param.getString("name");
                if (type.equals("common") || type.equals("form")) {
                    integrationParam.put(name, processTaskJson.get(value.replace("common#", StringUtils.EMPTY).replace("form#", StringUtils.EMPTY)));
                } else if (type.equals("integration") && MapUtils.isNotEmpty(resultJson)) {
                    integrationParam.put(name, resultJson.get(value.replace("integration#", StringUtils.EMPTY)));
                } else {// 常量
                    if (StringUtils.isNotBlank(value)) {
                        integrationParam.put(name, value);
                    }
                }
            }
        }
        return integrationParam;
    }

    /**
     * 获取回调信息
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
     */
    private void failPolicy(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo, String failedReason, JSONObject automaticInfo) {
        IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
            List<ProcessTaskStepVo> backStepList =
                    processTaskService.getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo);
            if (backStepList.size() == 1) {
                ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
                if (processHandler != null) {
                    processStepHandlerUtil.saveStepRemind(currentProcessTaskStepVo, nextProcessTaskStepVo.getId(), failedReason, ProcessTaskStepRemindType.AUTOMATIC_ERROR);
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
            processStepHandlerUtil.saveStepRemind(currentProcessTaskStepVo, nextStepIdList.get(0), failedReason, ProcessTaskStepRemindType.AUTOMATIC_ERROR);
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
        } else {
            // 人工处理
            try {
                processHandler.assign(currentProcessTaskStepVo);
            } catch (ProcessTaskException e) {
                logger.error(e.getMessage(), e);
            }
            processStepHandlerUtil.saveStepRemind(currentProcessTaskStepVo, currentProcessTaskStepVo.getId(), failedReason, ProcessTaskStepRemindType.AUTOMATIC_ERROR);
        }
    }
}
