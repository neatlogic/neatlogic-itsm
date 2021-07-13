/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.WorkAssignmentUnitVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.form.constvalue.FormAttributeAction;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.notify.dto.NotifyReceiverVo;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.constvalue.automatic.CallbackType;
import codedriver.framework.process.constvalue.automatic.FailPolicy;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.process.stepremind.core.ProcessTaskStepRemindTypeFactory;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.util.ConditionUtil;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.framework.util.TimeUtil;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.module.process.integration.handler.ProcessRequestFrom;
import codedriver.module.process.schedule.plugin.ProcessTaskAutomaticJob;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

    private final static Logger logger = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);

    private final Pattern pattern_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private IntegrationMapper integrationMapper;

    @Autowired
    private WorktimeMapper worktimeMapper;

    @Autowired
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private ProcessStepHandlerMapper processStepHandlerMapper;

    @Autowired
    private PriorityMapper priorityMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ChannelTypeMapper channelTypeMapper;
    @Autowired
    private CatalogMapper catalogMapper;

    @Override
    public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo,
                                                  Map<String, String> formAttributeActionMap, int mode) {
        Map<String, Object> formAttributeDataMap = processTaskVo.getFormAttributeDataMap();
        if (formAttributeDataMap == null) {
            formAttributeDataMap = new HashMap<>();
        }
        String formConfig = processTaskVo.getFormConfig();
        if (StringUtils.isNotBlank(formConfig)) {
            try {
                JSONObject formConfigObj = JSON.parseObject(formConfig);
                if (MapUtils.isNotEmpty(formConfigObj)) {
                    JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
                    if (CollectionUtils.isNotEmpty(controllerList)) {
                        List<String> currentUserProcessUserTypeList = new ArrayList<>();
                        List<String> currentUserTeamList = new ArrayList<>();
                        if (mode == 0) {
                            currentUserProcessUserTypeList.add(UserType.ALL.getValue());
                            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                                currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
                            }
                            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                                currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
                            }
                            currentUserTeamList =
                                    teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                        } else if (mode == 1) {
                            if (formAttributeActionMap == null) {
                                formAttributeActionMap = new HashMap<>();
                            }
                        }

                        for (int i = 0; i < controllerList.size(); i++) {
                            JSONObject attributeObj = controllerList.getJSONObject(i);
                            String action = FormAttributeAction.HIDE.getValue();
                            JSONObject config = attributeObj.getJSONObject("config");
                            if (mode == 0) {
                                if (MapUtils.isNotEmpty(config)) {
                                    List<String> authorityList =
                                            JSON.parseArray(config.getString("authorityConfig"), String.class);
                                    if (CollectionUtils.isNotEmpty(authorityList)) {
                                        for (String authority : authorityList) {
                                            String[] split = authority.split("#");
                                            if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                                if (currentUserProcessUserTypeList.contains(split[1])) {
                                                    action = FormAttributeAction.READ.getValue();
                                                    break;
                                                }
                                            } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue()
                                                    .equals(split[0])) {
                                                if (currentUserProcessUserTypeList.contains(split[1])) {
                                                    action = FormAttributeAction.READ.getValue();
                                                    break;
                                                }
                                            } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                                if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                                    action = FormAttributeAction.READ.getValue();
                                                    break;
                                                }
                                            } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                                if (currentUserTeamList.contains(split[1])) {
                                                    action = FormAttributeAction.READ.getValue();
                                                    break;
                                                }
                                            } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                                if (UserContext.get().getRoleUuidList().contains(split[1])) {
                                                    action = FormAttributeAction.READ.getValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (mode == 1) {
                                action = formAttributeActionMap.get(attributeObj.getString("uuid"));
                                if (StringUtils.isBlank(action)) {
                                    action = formAttributeActionMap.get("all");
                                }
                            }
                            if (FormAttributeAction.READ.getValue().equals(action)) {
                                attributeObj.put("isReadonly", true);
                            } else if (FormAttributeAction.HIDE.getValue().equals(action)) {
                                attributeObj.put("isHide", true);
                                formAttributeDataMap.remove(attributeObj.getString("uuid"));// 对于隐藏属性，不返回值
                                if (config != null) {
                                    config.remove("value");
                                    config.remove("defaultValueList");// 对于隐藏属性，不返回默认值
                                }
                            }
                        }
                        processTaskVo.setFormConfig(formConfigObj.toJSONString());
                    }
                }
            } catch (Exception ex) {
                logger.error("表单配置不是合法的JSON格式", ex);
            }
        }

    }

    @Override
    public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo) {
        if (StringUtils.isBlank(processTaskStepReplyVo.getContent())
                && StringUtils.isNotBlank(processTaskStepReplyVo.getContentHash())) {
            processTaskStepReplyVo.setContent(
                    selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepReplyVo.getContentHash()));
        }
        List<Long> fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepReplyVo.getId());
        if (CollectionUtils.isNotEmpty(fileIdList)) {
            processTaskStepReplyVo.setFileIdList(fileIdList);
            processTaskStepReplyVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }
        if (StringUtils.isNotBlank(processTaskStepReplyVo.getLcu())) {
            UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getLcu());
            if (user != null) {
                //使用新对象，防止缓存
                UserVo vo = new UserVo();
                BeanUtils.copyProperties(user, vo);
                processTaskStepReplyVo.setLcuVo(vo);
//                processTaskStepReplyVo.setLcuName(user.getUserName());
//                processTaskStepReplyVo.setLcuInfo(user.getUserInfo());
//                processTaskStepReplyVo.setLcuVipLevel(user.getVipLevel());
            }
        }
        UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getFcu());
        if (user != null) {
            //使用新对象，防止缓存
            UserVo vo = new UserVo();
            BeanUtils.copyProperties(user, vo);
            processTaskStepReplyVo.setFcuVo(vo);
//            processTaskStepReplyVo.setFcuName(user.getUserName());
//            processTaskStepReplyVo.setFcuInfo(user.getUserInfo());
//            processTaskStepReplyVo.setFcuVipLevel(user.getVipLevel());
        }
    }

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
                    audit.put("failedReason", failedReason);
                    if (FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
                        List<ProcessTaskStepVo> backStepList =
                                getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo.getId());
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

    @Override
    public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo,
                                              AutomaticConfigVo automaticConfigVo, JSONObject data, String type) {
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

    /**
     * 拼装入参数
     *
     * @param automaticConfigVo        自动化配置
     * @param currentProcessTaskStepVo 当前步骤
     * @return 参数对象
     */
    private JSONObject getIntegrationParam(AutomaticConfigVo automaticConfigVo,
                                           ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepVo stepVo = getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
        ProcessTaskVo processTaskVo = getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
        processTaskVo.setStartProcessTaskStep(getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
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
                    integrationParam.put(name, processTaskJson.get(value));
                } else if (type.equals("integration")) {
                    integrationParam.put(name, resultJson.get(value));
                } else {// 常量
                    //TODO linbq 设置参数映射值为外部调用返回结果时，type的值为constant（有bug），应该是integration才对,msjy poc时临时修改逻辑应付
                    Object paramValue = resultJson.get(value);
                    if (paramValue != null) {
                        integrationParam.put(name, paramValue);
                    }else {
                        integrationParam.put(name, value);
                    }
                }
            }
        }
        return integrationParam;
    }

    // @Override
    private ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId) {
        // 获取步骤信息
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);

        // 处理人列表
        List<ProcessTaskStepUserVo> majorUserList =
                processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        }
        List<ProcessTaskStepUserVo> minorUserList =
                processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
        processTaskStepVo.setMinorUserList(minorUserList);

        List<ProcessTaskStepWorkerVo> workerList =
                processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
                        processTaskStepVo.getProcessTaskId(), processTaskStepId);
        for (ProcessTaskStepWorkerVo workerVo : workerList) {
            if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                if (userVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                    workerVo.setName(userVo.getUserName());
                }
            } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                if (teamVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                    workerVo.setName(teamVo.getName());
                }
            } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                if (roleVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                    workerVo.setName(roleVo.getName());
                }
            }
        }
        processTaskStepVo.setWorkerList(workerList);

        return processTaskStepVo;
    }

   /* public static void main(String[] args) {
        Pattern pattern = Pattern.compile("(5|4).*");
        System.out.println(pattern.matcher("300").matches());
    }*/

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId)
            throws Exception {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        if (processTaskVo.getIsShow() != 1 && !AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName())) {
            throw new PermissionDeniedException();
        }
        if (processTaskStepId != null) {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
            }
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            if (!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
            processTaskVo.setCurrentProcessTaskStep(processTaskStepVo);
        }
        if (nextStepId != null) {
            ProcessTaskStepVo nextProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(nextStepId);
            if (nextProcessTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(nextStepId.toString());
            }
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(nextProcessTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(nextProcessTaskStepVo.getHandler());
            }
            if (!processTaskId.equals(nextProcessTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + nextStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
        }
        return processTaskVo;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, null);
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, null, null);
    }

    @Override
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId,
                                                                                       List<String> typeList) {
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList =
                processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
            if (typeList.contains(processTaskStepContentVo.getType())) {
                ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
                parseProcessTaskStepReply(processTaskStepReplyVo);
                processTaskStepReplyList.add(processTaskStepReplyVo);
            }
        }
        return processTaskStepReplyList;
    }

    @Override
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(Long processTaskId, String processStepUuid) {
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList =
                processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if (CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
            for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
                if (WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList =
                            JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for (String stepUuid : processStepUuidList) {
                        if (processStepUuid.equals(stepUuid)) {
                            List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(
                                    workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                            if (CollectionUtils.isEmpty(majorList)) {
                                ProcessTaskStepVo assignableWorkerStep = processTaskMapper
                                        .getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                                assignableWorkerStep
                                        .setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
                                assignableWorkerStepList.add(assignableWorkerStep);
                            }
                        }
                    }
                }
            }
            return assignableWorkerStepList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(String processUuid, String processStepUuid) {
        List<ProcessStepWorkerPolicyVo> processStepWorkerPolicyList =
                processMapper.getProcessStepWorkerPolicyListByProcessUuid(processUuid);
        if (CollectionUtils.isNotEmpty(processStepWorkerPolicyList)) {
            List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
            for (ProcessStepWorkerPolicyVo workerPolicyVo : processStepWorkerPolicyList) {
                if (WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList =
                            JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for (String stepUuid : processStepUuidList) {
                        if (processStepUuid.equals(stepUuid)) {
                            ProcessStepVo processStep =
                                    processMapper.getProcessStepByUuid(workerPolicyVo.getProcessStepUuid());
                            ProcessTaskStepVo assignableWorkerStep = new ProcessTaskStepVo(processStep);
                            assignableWorkerStep.setIsAutoGenerateId(false);
                            assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
                            assignableWorkerStepList.add(assignableWorkerStep);
                        }
                    }
                }
            }
            return assignableWorkerStepList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(Long processTaskStepId,
                                                                                       String worktimeUuid) {
        List<ProcessTaskSlaTimeVo> slaTimeList = new ArrayList<>();
        List<ProcessTaskSlaVo> processTaskSlaList =
                processTaskMapper.getProcessTaskSlaByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskSlaVo processTaskSlaVo : processTaskSlaList) {
            ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaVo.getSlaTimeVo();
            if (processTaskSlaTimeVo != null) {
                long nowTime = System.currentTimeMillis();
                processTaskSlaTimeVo.setName(processTaskSlaVo.getName());
                processTaskSlaTimeVo.setSlaId(processTaskSlaVo.getId());
                if (processTaskSlaTimeVo.getExpireTime() != null) {
                    long timeLeft = 0L;
                    long expireTime = processTaskSlaTimeVo.getExpireTime().getTime();
                    if (nowTime < expireTime) {
                        timeLeft = worktimeMapper.calculateCostTime(worktimeUuid, nowTime, expireTime);
                    } else if (nowTime > expireTime) {
                        timeLeft = -worktimeMapper.calculateCostTime(worktimeUuid, expireTime, nowTime);
                    }
                    processTaskSlaTimeVo.setTimeLeft(timeLeft);
                }
                if (processTaskSlaTimeVo.getRealExpireTime() != null) {
                    long realTimeLeft = processTaskSlaTimeVo.getRealExpireTime().getTime() - nowTime;
                    processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
                }
                slaTimeList.add(processTaskSlaTimeVo);
            }
        }
        return slaTimeList;
    }

    @Override
    public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,
                ProcessFlowDirection.FORWARD.getValue());
        for (ProcessTaskStepVo processTaskStep : nextStepList) {
            if (StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                processTaskStep.setName(processTaskStep.getAliasName());
                processTaskStep.setFlowDirection("");
            } else {
                processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
            }
            resultList.add(processTaskStep);
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskStepVo> getBackwardNextStepListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,
                ProcessFlowDirection.BACKWARD.getValue());
        for (ProcessTaskStepVo processTaskStep : nextStepList) {
            if (!Objects.equals(processTaskStep.getIsActive(), 0)) {
                if (StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                    processTaskStep.setName(processTaskStep.getAliasName());
                    processTaskStep.setFlowDirection("");
                } else {
                    processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
                }
                resultList.add(processTaskStep);
            }
        }
        return resultList;
    }

    @Override
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper
                .getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        } else {
            List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
            for (ProcessTaskStepWorkerVo workerVo : workerList) {
                if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                    UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                    if (userVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                        workerVo.setName(userVo.getUserName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                    TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                    if (teamVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                        workerVo.setName(teamVo.getName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                    RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                    if (roleVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                        workerVo.setName(roleVo.getName());
                    }
                }
            }
            processTaskStepVo.setWorkerList(workerList);
        }
        processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(),
                ProcessUserType.MINOR.getValue()));
    }

    @Override
    public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo oldReplyVo) {
        if (oldReplyVo == null) {
            return false;
        }
        String content = jsonObj.getString("content");
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if (content == null && fileIdList == null) {
            return false;
        }
        Long processTaskId = oldReplyVo.getProcessTaskId();
        Long processTaskStepId = oldReplyVo.getProcessTaskStepId();
        boolean isUpdate = false;
        // 获取上传附件id列表
        List<Long> oldFileIdList = new ArrayList<>();
        // 获取上报描述内容
        String oldContent = null;
        Long oldContentId = null;
        if (oldReplyVo.getId() != null) {
            parseProcessTaskStepReply(oldReplyVo);
            oldContentId = oldReplyVo.getId();
            oldContent = oldReplyVo.getContent();
            oldFileIdList = oldReplyVo.getFileIdList();
        }

        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(oldContent)) {
            if (content.equals(oldContent)) {
                jsonObj.remove("content");
            } else {
                jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContent);
                isUpdate = true;
                ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
                processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
                if (oldContentId == null) {
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.TASK_START.getValue());
                    processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                    oldContentId = processTaskStepContentVo.getId();
                } else {
                    processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, contentVo.getHash()));
                }
            }
        } else if (StringUtils.isNotBlank(content)) {
            isUpdate = true;
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
            if (oldContentId == null) {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.TASK_START.getValue());
                processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                oldContentId = processTaskStepContentVo.getId();
            } else {
                processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, contentVo.getHash()));
            }
        } else if (StringUtils.isNotBlank(oldContent)) {
            isUpdate = true;
            jsonObj.remove("content");
            jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContent);
            processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, null));
            if (CollectionUtils.isEmpty(fileIdList)) {
            } else {

            }
        } else {
            jsonObj.remove("content");
        }

        /* 保存新附件uuid **/
        if (CollectionUtils.isNotEmpty(fileIdList) && CollectionUtils.isNotEmpty(oldFileIdList)) {
            if (Objects.equals(oldFileIdList, fileIdList)) {
                jsonObj.remove("fileIdList");
            } else {
                processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
                jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), JSON.toJSONString(oldFileIdList));
                isUpdate = true;
                if (oldContentId == null) {
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.TASK_START.getValue());
                    processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                    oldContentId = processTaskStepContentVo.getId();
                }
                ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
                processTaskStepFileVo.setProcessTaskId(processTaskId);
                processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
                processTaskStepFileVo.setContentId(oldContentId);
                for (Long fileId : fileIdList) {
                    if (fileMapper.getFileById(fileId) == null) {
                        throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        } else if (CollectionUtils.isNotEmpty(fileIdList)) {
            isUpdate = true;
            if (oldContentId == null) {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.TASK_START.getValue());
                processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                oldContentId = processTaskStepContentVo.getId();
            }
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(processTaskId);
            processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
            processTaskStepFileVo.setContentId(oldContentId);
            for (Long fileId : fileIdList) {
                if (fileMapper.getFileById(fileId) == null) {
                    throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
                }
                processTaskStepFileVo.setFileId(fileId);
                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
            }
        } else if (CollectionUtils.isNotEmpty(oldFileIdList)) {
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
            jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), JSON.toJSONString(oldFileIdList));
            isUpdate = true;
            jsonObj.remove("fileIdList");
        } else {
            jsonObj.remove("fileIdList");
        }

        if (oldContentId != null && StringUtils.isBlank(content) && CollectionUtils.isEmpty(fileIdList)) {
            processTaskMapper.deleteProcessTaskStepContentById(oldContentId);
        }
        return isUpdate;
    }

    /**
     * 检查当前用户是否配置该权限
     *
     * @param processTaskStepVo 步骤信息
     * @param owner             所有人
     * @param reporter          上报人
     * @param operationType     操作类型
     * @param userUuid          用户uuid
     * @return 是否拥有权限
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, String owner, String reporter,
                                                  ProcessTaskOperationType operationType, String userUuid) {
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        JSONArray authorityList = (JSONArray) JSONPath.read(stepConfig, "authorityList");
        if (CollectionUtils.isEmpty(authorityList)) {
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            ProcessStepHandlerVo processStepHandlerConfig =
                    processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
            JSONObject globalConfig = processStepUtilHandler
                    .makeupConfig(processStepHandlerConfig != null ? processStepHandlerConfig.getConfig() : null);
            authorityList = (JSONArray) JSONPath.read(JSON.toJSONString(globalConfig), "authorityList");
        }

        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isNotEmpty(authorityList)) {
            return checkOperationAuthIsConfigured(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(),
                    owner, reporter, operationType, authorityList, userUuid);
        }
        return false;
    }

    /**
     * 检查当前用户是否配置该权限
     *
     * @param processTaskVo 作业信息
     * @param operationType 操作类型
     * @param userUuid      用户uuid
     * @return 是否拥有权限
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskVo processTaskVo, ProcessTaskOperationType operationType,
                                                  String userUuid) {
        String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
        JSONArray authorityList = (JSONArray) JSONPath.read(config, "process.processConfig.authorityList");
        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isNotEmpty(authorityList)) {
            return checkOperationAuthIsConfigured(processTaskVo.getId(), null, processTaskVo.getOwner(),
                    processTaskVo.getReporter(), operationType, authorityList, userUuid);
        }
        return false;
    }

    private boolean checkOperationAuthIsConfigured(Long processTaskId, Long processTaskStepId, String owner,
                                                   String reporter, ProcessTaskOperationType operationType, JSONArray authorityList, String userUuid) {
        for (int i = 0; i < authorityList.size(); i++) {
            JSONObject authorityObj = authorityList.getJSONObject(i);
            String action = authorityObj.getString("action");
            if (operationType.getValue().equals(action)) {
                JSONArray acceptList = authorityObj.getJSONArray("acceptList");
                if (CollectionUtils.isNotEmpty(acceptList)) {
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                    List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(processTaskId);
                    processTaskStepUserVo.setProcessTaskStepId(processTaskStepId);
                    processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                    for (int j = 0; j < acceptList.size(); j++) {
                        String accept = acceptList.getString(j);
                        String[] split = accept.split("#");
                        if (GroupSearch.COMMON.getValue().equals(split[0])) {
                            if (UserType.ALL.getValue().equals(split[1])) {
                                return true;
                            }
                        } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                            if (ProcessUserType.OWNER.getValue().equals(split[1])) {
                                if (userUuid.equals(owner)) {
                                    return true;
                                }
                            } else if (ProcessUserType.REPORTER.getValue().equals(split[1])) {
                                if (userUuid.equals(reporter)) {
                                    return true;
                                }
                            } else if (ProcessUserType.MAJOR.getValue().equals(split[1])) {
                                processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
                                if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                                    return true;
                                }
                            } else if (ProcessUserType.MINOR.getValue().equals(split[1])) {
                                processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                                if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                                    return true;
                                }
                            }
                        } else if (GroupSearch.USER.getValue().equals(split[0])) {
                            if (userUuid.equals(split[1])) {
                                return true;
                            }
                        } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                            if (teamUuidList.contains(split[1])) {
                                return true;
                            }
                        } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                            if (roleUuidList.contains(split[1])) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取工单中当前用户能撤回的步骤列表
     *
     * @param processTaskVo 作业信息
     * @param userUuid      用户uuid
     * @return 步骤信息
     */
    @Override
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> stepVoList =
                processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskStepVo stepVo : stepVoList) {
            /** 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                resultSet.addAll(getRetractableStepListByProcessTaskStepId(processTaskVo, stepVo.getId(), userUuid));
            }
        }
        return resultSet;
    }

    /**
     * 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     *
     * @param processTaskVo     作业信息
     * @param processTaskStepId 步骤id
     * @param userUuid          用户uuid
     * @return 步骤列表
     */
    @Override
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(ProcessTaskVo processTaskVo,
                                                                             Long processTaskStepId, String userUuid) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        /** 所有前置步骤 **/
        List<ProcessTaskStepVo> fromStepList = processTaskMapper.getFromProcessTaskStepByToId(processTaskStepId);
        /** 找到所有已完成步骤 **/
        for (ProcessTaskStepVo fromStep : fromStepList) {
            IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(fromStep.getHandler());
            if (handler != null) {
                if (ProcessStepMode.MT == handler.getMode()) {// 手动处理节点
                    if (checkOperationAuthIsConfigured(fromStep, processTaskVo.getOwner(), processTaskVo.getReporter(),
                            ProcessTaskOperationType.STEP_RETREAT, userUuid)) {
                        resultList.add(fromStep);
                    }
                } else {// 自动处理节点，继续找前置节点
                    resultList
                            .addAll(getRetractableStepListByProcessTaskStepId(processTaskVo, fromStep.getId(), userUuid));
                }
            } else {
                throw new ProcessStepHandlerNotFoundException(fromStep.getHandler());
            }
        }
        return resultList;
    }

    /**
     * 获取工单中当前用户能催办的步骤列表
     *
     * @param processTaskVo 作业信息
     * @param userUuid      用户uuid
     * @return 步骤列表
     */
    @Override
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo, String userUuid) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_URGE, userUuid)) {
            List<ProcessTaskStepVo> processTaskStepList =
                    processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                if (processTaskStep.getIsActive() == 1) {
                    resultList.add(processTaskStep);
                }
            }
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepRemindVo> processTaskStepRemindList =
                processTaskMapper.getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepRemindVo processTaskStepRemindVo : processTaskStepRemindList) {
            processTaskStepRemindVo
                    .setActionName(ProcessTaskStepRemindTypeFactory.getText(processTaskStepRemindVo.getAction()));
            String contentHash = processTaskStepRemindVo.getContentHash();
            if (StringUtils.isNotBlank(contentHash)) {
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
                if(StringUtils.isNotBlank(content)) {
                    /** 有图片标签才显式点击详情 **/
                    if(content.contains("<figure class=\"image\">") && content.contains("</figure>")) {
                        processTaskStepRemindVo.setDetail(content);
                    }
                    processTaskStepRemindVo.setContent(pattern_html.matcher(content).replaceAll(""));
                }
            }
            UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepRemindVo.getFcu());
            if (userVo != null) {
                UserVo vo = new UserVo();
                BeanUtils.copyProperties(userVo, vo);
                processTaskStepRemindVo.setFcuVo(vo);
            }
        }
        return processTaskStepRemindList;
    }

    @Override
    public Set<ProcessTaskStepVo> getTransferableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> stepVoList =
                processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskStepVo stepVo : stepVoList) {
            /** 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                if (checkOperationAuthIsConfigured(stepVo, processTaskVo.getOwner(), processTaskVo.getReporter(),
                        ProcessTaskOperationType.STEP_TRANSFER, userUuid)) {
                    resultSet.add(stepVo);
                }
            }
        }
        return resultSet;
    }

    @Override
    public ProcessTaskVo getProcessTaskDetailById(Long processTaskId) {
        // 获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        // 判断当前用户是否关注该工单
        if (processTaskVo != null
                && processTaskMapper.checkProcessTaskFocusExists(processTaskId, UserContext.get().getUserUuid()) > 0) {
            processTaskVo.setIsFocus(1);
        }
        // 获取工单流程图信息
        ProcessTaskConfigVo processTaskConfig =
                selectContentByHashMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
        if (processTaskConfig == null) {
            throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
        }
        processTaskVo.setConfig(processTaskConfig.getConfig());

        // 优先级
        PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
        if (priorityVo == null) {
            priorityVo = new PriorityVo();
            priorityVo.setUuid(processTaskVo.getPriorityUuid());
        }
        processTaskVo.setPriority(priorityVo);
        // 上报服务路径
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if (channelVo != null) {
            CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
            if (catalogVo != null) {
                List<CatalogVo> catalogList =
                        catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
                List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
                nameList.add(channelVo.getName());
                processTaskVo.setChannelPath(String.join("/", nameList));
            }
            ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if (channelTypeVo == null) {
                channelTypeVo = new ChannelTypeVo();
                channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
            }
            try {
                processTaskVo.setChannelType(channelTypeVo.clone());
            } catch (CloneNotSupportedException ignored) {
            }
        }
        // 耗时
        if (processTaskVo.getEndTime() != null) {
            long timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(),
                    processTaskVo.getStartTime().getTime(), processTaskVo.getEndTime().getTime());
            processTaskVo.setTimeCost(timeCost);
            processTaskVo.setTimeCostStr(TimeUtil.millisecondsTransferMaxTimeUnit(timeCost));
        }

        // 获取工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent =
                    selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                processTaskVo.setFormConfig(formContent);
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList =
                        processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(),
                            processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        /** 上报人公司列表 **/
        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTaskVo.getOwner());
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            Set<Long> idSet = new HashSet<>();
            List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
            for (TeamVo teamVo : teamList) {
                List<TeamVo> companyList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(),
                        TeamLevel.COMPANY.getValue());
                if (CollectionUtils.isNotEmpty(companyList)) {
                    for (TeamVo team : companyList) {
                        if (!idSet.contains(team.getId())) {
                            idSet.add(team.getId());
                            processTaskVo.getOwnerCompanyList().add(team);
                        }
                    }
                }
            }
        }
        /** 获取评分信息 */
        String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(processTaskId);
        processTaskVo.setScoreInfo(scoreInfo);

        /** 转报数据 **/
        Long fromProcessTaskId = processTaskMapper.getFromProcessTaskIdByToProcessTaskId(processTaskId);
        if (fromProcessTaskId != null) {
            processTaskVo.getTranferReportProcessTaskList().add(getFromProcessTasById(fromProcessTaskId));
        }
        List<Long> toProcessTaskIdList = processTaskMapper.getToProcessTaskIdListByFromProcessTaskId(processTaskId);
        for (Long toProcessTaskId : toProcessTaskIdList) {
            ProcessTaskVo toProcessTaskVo = processTaskMapper.getProcessTaskBaseInfoById(toProcessTaskId);
            if (toProcessTaskVo != null) {
                toProcessTaskVo.setTranferReportDirection("to");
                ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                if (channel != null) {
                    ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
                    if (channelTypeVo == null) {
                        channelTypeVo = new ChannelTypeVo();
                        channelTypeVo.setUuid(channel.getChannelTypeUuid());
                    }
                    try {
                        processTaskVo.setChannelType(channelTypeVo.clone());
                    } catch (CloneNotSupportedException ignored) {
                    }
                }
                processTaskVo.getTranferReportProcessTaskList().add(toProcessTaskVo);
            }
        }
        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId));
        /* 工单关注人列表 **/
        List<String> focusUserUuidList = processTaskMapper.getFocusUserListByTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(focusUserUuidList)) {
            processTaskVo.setFocusUserUuidList(focusUserUuidList);
        }
        /* 查询当前用户是否有权限修改工单关注人 **/
        int canEditFocusUser = new ProcessAuthManager
                .TaskOperationChecker(processTaskId, ProcessTaskOperationType.TASK_FOCUSUSER_UPDATE).build()
                .check() ? 1 : 0;
        processTaskVo.setCanEditFocusUser(canEditFocusUser);

        return processTaskVo;
    }

    @Override
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        // 获取开始步骤id
        ProcessTaskStepVo startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
        ProcessTaskStepReplyVo comment = new ProcessTaskStepReplyVo();
        // 获取上报描述内容
        List<Long> fileIdList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList =
                processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startProcessTaskStepVo.getId());
        for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskOperationType.TASK_START.getValue().equals(processTaskStepContent.getType())) {
                fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                comment.setContent(selectContentByHashMapper
                        .getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
                break;
            }
        }
        // 附件
        if (CollectionUtils.isNotEmpty(fileIdList)) {
            comment.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }
        startProcessTaskStepVo.setComment(comment);
        /** 当前步骤特有步骤信息 **/
        IProcessStepInternalHandler startProcessStepUtilHandler =
                ProcessStepInternalHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if (startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessTaskStepVo
                .setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInfo(startProcessTaskStepVo));
        return startProcessTaskStepVo;
    }

    @Override
    public ProcessTaskStepVo getCurrentProcessTaskStepDetail(ProcessTaskStepVo currentProcessTaskStep) {
        if (currentProcessTaskStep.getId() == null) {
            return null;
        }
        // 获取步骤信息
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStep.getId());
        if (processTaskStepVo == null) {
            return null;
        }
        processTaskStepVo.setParamObj(currentProcessTaskStep.getParamObj());
        List<ProcessTaskStepWorkerVo> workerList =
                processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
                        processTaskStepVo.getProcessTaskId(), currentProcessTaskStep.getId());
        for (ProcessTaskStepWorkerVo workerVo : workerList) {
            if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                if (userVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                    workerVo.setName(userVo.getUserName());
                }
            } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                if (teamVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                    workerVo.setName(teamVo.getName());
                }
            } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                if (roleVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                    workerVo.setName(roleVo.getName());
                }
            }
        }
        processTaskStepVo.setWorkerList(workerList);
        List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStep.getId(), null);
        processTaskStepVo.setUserList(userList);
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processTaskStepVo.setHandlerStepInfo(handler.getHandlerStepInitInfo(processTaskStepVo));
        processTaskStepVo.setCurrentSubtaskVo(currentProcessTaskStep.getCurrentSubtaskVo());
        return processTaskStepVo;
    }

    @Override
    public ProcessTaskVo getFromProcessTasById(Long processTaskId) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo != null) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
            if (channelVo != null) {
                ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
                if (channelTypeVo == null) {
                    channelTypeVo = new ChannelTypeVo();
                    channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
                }
                try {
                    processTaskVo.setChannelType(channelTypeVo.clone());
                } catch (CloneNotSupportedException ignored) {
                }
            }
            // 获取工单表单信息
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
                String formContent =
                        selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                if (StringUtils.isNotBlank(formContent)) {
                    processTaskVo.setFormConfig(formContent);
                    List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList =
                            processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
                    for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                        processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(),
                                processTaskFormAttributeDataVo.getDataObj());
                    }
                }
            }

            processTaskVo.setStartProcessTaskStep(getStartProcessTaskStepByProcessTaskId(processTaskId));
            processTaskVo.setTranferReportDirection("from");
        }
        return processTaskVo;
    }

    /**
     * 获取所有工单干系人信息，用于通知接收人
     *
     * @param currentProcessTaskStepVo 当前步骤
     * @param receiverMap              通知人
     */
    @Override
    public void getReceiverMap(ProcessTaskStepVo currentProcessTaskStepVo,
                               Map<String, List<NotifyReceiverVo>> receiverMap) {
        ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            /** 上报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getOwner())) {
                receiverMap.computeIfAbsent(ProcessUserType.OWNER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskVo.getOwner()));
            }
            /** 代报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getReporter())) {
                receiverMap.computeIfAbsent(ProcessUserType.REPORTER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskVo.getReporter()));
            }
        }
        ProcessTaskStepUserVo processTaskStepUser = new ProcessTaskStepUserVo();
        processTaskStepUser.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        processTaskStepUser.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        /** 主处理人 **/
        processTaskStepUser.setUserType(ProcessUserType.MAJOR.getValue());
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
        for (ProcessTaskStepUserVo processTaskStepUserVo : majorUserList) {
            receiverMap.computeIfAbsent(ProcessUserType.MAJOR.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserVo().getUuid()));
        }
        /** 子任务处理人 **/
        processTaskStepUser.setUserType(ProcessUserType.MINOR.getValue());
        List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
        for (ProcessTaskStepUserVo processTaskStepUserVo : minorUserList) {
            receiverMap.computeIfAbsent(ProcessUserType.MINOR.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserVo().getUuid()));
        }
        /** 待处理人 **/
        List<ProcessTaskStepWorkerVo> workerList =
                processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
                        currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId());
        for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : workerList) {
            receiverMap.computeIfAbsent(ProcessUserType.WORKER.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(processTaskStepWorkerVo.getType(), processTaskStepWorkerVo.getUuid()));
        }

        /** 工单关注人 */
        List<String> focusUserList =
                processTaskMapper.getFocusUsersOfProcessTask(currentProcessTaskStepVo.getProcessTaskId());
        for (String user : focusUserList) {
            String[] split = user.split("#");
            receiverMap.computeIfAbsent(ProcessUserType.FOCUS_USER.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(split[0], split[1]));
        }

        /** 异常处理人 **/
        if (StringUtils.isNotBlank(currentProcessTaskStepVo.getConfig())) {
            String defaultWorker =
                    (String)JSONPath.read(currentProcessTaskStepVo.getConfig(), "workerPolicyConfig.defaultWorker");
            if (StringUtils.isNotBlank(defaultWorker)) {
                String[] split = defaultWorker.split("#");
                receiverMap.computeIfAbsent(ProcessUserType.DEFAULT_WORKER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(split[0], split[1]));
            }
        }
    }

    /**
     * 设置步骤当前用户的暂存数据
     *
     * @param processTaskVo     任务信息
     * @param processTaskStepVo 步骤信息
     */
    @Override
    public void setTemporaryData(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepVo.getId());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                /** 表单属性 **/
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    Map<String, Object> formAttributeDataMap = new HashMap<>();
                    for (int i = 0; i < formAttributeDataList.size(); i++) {
                        JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                        formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"), formAttributeDataObj.get("dataList"));
                    }
//                    processTaskStepVo.setFormAttributeDataMap(formAttributeDataMap);
                    processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
                }
                /** 描述及附件 **/
                ProcessTaskStepReplyVo commentVo = new ProcessTaskStepReplyVo();
                String content = dataObj.getString("content");
                commentVo.setContent(content);
                List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("fileIdList")), Long.class);
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    commentVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
                }
                processTaskStepVo.setComment(commentVo);
                /** 当前步骤特有步骤信息 **/
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (handlerStepInfo != null) {
                    processTaskStepVo.setHandlerStepInfo(handlerStepInfo);
                }
                /** 优先级 **/
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    processTaskVo.setPriorityUuid(priorityUuid);
                    PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priorityUuid);
                    if (priorityVo == null) {
                        priorityVo = new PriorityVo();
                        priorityVo.setUuid(priorityUuid);
                    }
                    processTaskVo.setPriority(priorityVo);
                }
                /** 标签列表 **/
                List<String> tagList = JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("tagList")), String.class);
                if(tagList != null){
                    processTaskVo.setTagList(tagList);
                }
                /** 工单关注人列表 **/
                List<String> focusUserUuidList = JSON.parseArray(dataObj.getString("focusUserUuidList"),String.class);
                if(CollectionUtils.isNotEmpty(focusUserUuidList)){
                    processTaskVo.setFocusUserUuidList(focusUserUuidList);
                }
            }
        }
    }


    /**
     * 查询待处理的工单，构造"用户uuid->List<工单字段中文名->值>"的map集合
     * @param conditionMap 工单查询条件
     * @return "用户uuid->List<工单字段中文名->值>"的map集合
     */
    @Override
    public Map<String,List<Map<String,Object>>> getProcessingUserTaskMapByCondition(Map<String,Object> conditionMap) {

        Map<String, List<Map<String, Object>>> userTaskMap = new HashMap<>();
        List<UserVo> userList = (List<UserVo>) conditionMap.get("userList");
        /** 以处理组中的用户为单位，查询每个用户的待办工单 **/
        if (CollectionUtils.isNotEmpty(userList)) {
            for (UserVo user : userList) {
                getConditionMap(conditionMap, user);
                List<Map<String, Object>> taskList = new ArrayList<>();
                /** 查询工单 **/
                List<Long> taskIdList = processTaskMapper.getProcessingTaskIdListByCondition(conditionMap);
                if (CollectionUtils.isNotEmpty(taskIdList)) {
                    List<ProcessTaskVo> processTaskList = processTaskMapper.getTaskListByIdList(taskIdList);
                    for (ProcessTaskVo processTaskVo : processTaskList) {
                        Map<String, Object> map = new HashMap<>();
                        for (IProcessTaskColumn column : ProcessTaskColumnFactory.columnComponentMap.values()) {
                            if (!column.getDisabled() && column.getIsShow() && column.getIsExport()) {
                                map.put(column.getDisplayName(), column.getSimpleValue(processTaskVo));
                            }
                        }
                        taskList.add(map);
                    }
                }
                if (CollectionUtils.isNotEmpty(taskList)) {
                    userTaskMap.put(user.getUuid(), taskList);
                }
            }
        }

        return userTaskMap;
    }

    /**
     * 查询每个用户待处理的工单数量，构造"用户uuid->工单数"的map集合
     * @param conditionMap 工单查询条件
     * @return "用户uuid->工单数"的map集合
     */
    @Override
    public Map<String, Integer> getProcessingUserTaskCountByCondition(Map<String, Object> conditionMap) {
        Map<String, Integer> userTaskMap = new HashMap<>();
        List<UserVo> userList = (List<UserVo>) conditionMap.get("userList");
        /* 以处理组中的用户为单位，查询每个用户的待办工单数量 **/
        if (CollectionUtils.isNotEmpty(userList)) {
            for (UserVo user : userList) {
                getConditionMap(conditionMap, user);
                int taskCount = processTaskMapper.getProcessingTaskCountByCondition(conditionMap);
                if (taskCount > 0) {
                    userTaskMap.put(user.getUuid(), taskCount);
                }
            }
        }
        return userTaskMap;
    }

    /**
     * 把查询用户的待处理工单需要的userUuid、teamUuidList、roleUuidList条件put到conditionMap
     * @param conditionMap 查询条件
     * @param user 用户
     */
    private void getConditionMap(Map<String, Object> conditionMap, UserVo user) {
        conditionMap.remove("teamUuidList");
        conditionMap.remove("roleUuidList");
        List<String> teamUuidList = user.getTeamUuidList();
        List<String> roleUuidList = user.getRoleUuidList();
        conditionMap.put("userUuid", user.getUuid());
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            conditionMap.put("teamUuidList", teamUuidList);
        }
        if (CollectionUtils.isNotEmpty(roleUuidList)) {
            conditionMap.put("roleUuidList", roleUuidList);
        }
    }
}
