/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.*;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.notify.dto.NotifyReceiverVo;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.process.stepremind.core.ProcessTaskStepRemindTypeFactory;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.framework.util.TimeUtil;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
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

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

    private final static Logger logger = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);

    private final Pattern pattern_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private ProcessTaskStepTaskMapper processTaskStepTaskMapper;

    @Resource
    private WorktimeMapper worktimeMapper;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessStepHandlerMapper processStepHandlerMapper;

    @Resource
    private PriorityMapper priorityMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChannelTypeMapper channelTypeMapper;
    @Resource
    private CatalogMapper catalogMapper;
    @Resource
    private ProcessTaskAgentService processTaskAgentService;
//    @Override
//    public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo,
//                                                  Map<String, String> formAttributeActionMap, int mode) {
//        Map<String, Object> formAttributeDataMap = processTaskVo.getFormAttributeDataMap();
//        if (formAttributeDataMap == null) {
//            formAttributeDataMap = new HashMap<>();
//        }
//        JSONObject formConfigObj = processTaskVo.getFormConfig();
//        if (MapUtils.isNotEmpty(formConfigObj)) {
//            JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
//            if (CollectionUtils.isNotEmpty(controllerList)) {
//                List<String> currentUserProcessUserTypeList = new ArrayList<>();
//                AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
//                List<String> currentUserTeamList = authenticationInfoVo.getTeamUuidList();
//                List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
//                if (mode == 0) {
//                    currentUserProcessUserTypeList.add(UserType.ALL.getValue());
//                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
//                        currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
//                    }
//                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
//                        currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
//                    }
//                } else if (mode == 1) {
//                    if (formAttributeActionMap == null) {
//                        formAttributeActionMap = new HashMap<>();
//                    }
//                }
//
//                for (int i = 0; i < controllerList.size(); i++) {
//                    JSONObject attributeObj = controllerList.getJSONObject(i);
//                    String action = FormAttributeAction.HIDE.getValue();
//                    JSONObject config = attributeObj.getJSONObject("config");
//                    if (mode == 0) {
//                        if (MapUtils.isNotEmpty(config)) {
//                            List<String> authorityList =
//                                    JSON.parseArray(config.getString("authorityConfig"), String.class);
//                            if (CollectionUtils.isNotEmpty(authorityList)) {
//                                for (String authority : authorityList) {
//                                    String[] split = authority.split("#");
//                                    if (GroupSearch.COMMON.getValue().equals(split[0])) {
//                                        if (currentUserProcessUserTypeList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue()
//                                            .equals(split[0])) {
//                                        if (currentUserProcessUserTypeList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.USER.getValue().equals(split[0])) {
//                                        if (UserContext.get().getUserUuid(true).equals(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
//                                        if (currentUserTeamList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
//                                        if (roleUuidList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else if (mode == 1) {
//                        action = formAttributeActionMap.get(attributeObj.getString("uuid"));
//                        if (StringUtils.isBlank(action)) {
//                            action = formAttributeActionMap.get("all");
//                        }
//                    }
//                    if (FormAttributeAction.READ.getValue().equals(action)) {
//                        attributeObj.put("isReadonly", true);
//                    } else if (FormAttributeAction.HIDE.getValue().equals(action)) {
//                        attributeObj.put("isHide", true);
//                        formAttributeDataMap.remove(attributeObj.getString("uuid"));// 对于隐藏属性，不返回值
//                        if (config != null) {
//                            config.remove("value");
//                            config.remove("defaultValueList");// 对于隐藏属性，不返回默认值
//                        }
//                    }
//                }
//                processTaskVo.setFormConfig(formConfigObj);
//            }
//        }
//
//    }

    @Override
    public void setProcessTaskFormInfo(ProcessTaskVo processTaskVo) {
        Long processTaskId = processTaskVo.getId();
        if (processTaskId != null) {
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskVo.getId());
            if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
                String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                if (StringUtils.isNotBlank(formContent)) {
                    processTaskVo.setFormConfig(JSONObject.parseObject(formContent));
                    List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskVo.getId());
                    for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                        processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                    }
                    processTaskVo.setProcessTaskFormAttributeDataList(processTaskFormAttributeDataList);
                    // 获取工单流程图信息
                    String taskConfig = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                    JSONArray formConfigAuthorityList = (JSONArray) JSONPath.read(taskConfig, "process.formConfig.authorityList");
                    processTaskVo.setFormConfigAuthorityList(formConfigAuthorityList);
                    List<String> formAttributeHideList = getFormConfigAuthorityConfig(processTaskVo);
                    processTaskVo.setFormAttributeHideList(formAttributeHideList);
                }
            }
        } else {
            String processUuid = processTaskVo.getProcessUuid();
            if (StringUtils.isNotBlank(processUuid)) {
                ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
                if (processVo != null) {
                    String formUuid = processVo.getFormUuid();
                    if (StringUtils.isNotBlank(formUuid)) {
                        FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(formUuid);
                        if (formVersion == null) {
                            throw new FormActiveVersionNotFoundExcepiton(formUuid);
                        }
                        processTaskVo.setFormConfig(JSONObject.parseObject(formVersion.getFormConfig()));
                        JSONObject processConfig = processVo.getConfig();
                        if (MapUtils.isNotEmpty(processConfig)) {
                            JSONObject process = processConfig.getJSONObject("process");
                            if (MapUtils.isNotEmpty(process)) {
                                JSONObject formConfig = process.getJSONObject("formConfig");
                                if (MapUtils.isNotEmpty(formConfig)) {
                                    JSONArray authorityList = formConfig.getJSONArray("authorityList");
                                    processTaskVo.setFormConfigAuthorityList(authorityList);
                                }
                            }
                        }
                        List<String> formAttributeHideList = getFormConfigAuthorityConfig(processTaskVo);
                        processTaskVo.setFormAttributeHideList(formAttributeHideList);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getFormConfigAuthorityConfig(ProcessTaskVo processTaskVo) {
        List<String> resultList = new ArrayList<>();
        JSONObject formConfigObj = processTaskVo.getFormConfig();
        if (MapUtils.isNotEmpty(formConfigObj)) {
            JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
            if (CollectionUtils.isNotEmpty(controllerList)) {
                List<String> userUuidList = new ArrayList<>();
                String userUuid = UserContext.get().getUserUuid(true);
                userUuidList.add(userUuid);
                List<String> fromUserUuidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(userUuid, processTaskVo.getChannelUuid());
                userUuidList.addAll(fromUserUuidList);
//                String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(userUuid, "processtask");
//                if (StringUtils.isNotBlank(agentUuid)) {
//                    userUuidList.add(agentUuid);
//                }
                AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuidList);
                List<String> currentUserProcessUserTypeList = new ArrayList<>();
                currentUserProcessUserTypeList.add(UserType.ALL.getValue());
                if (processTaskVo.getId() != null) {
                    if (userUuidList.contains(processTaskVo.getOwner())) {
                        currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
                    }
                    if (userUuidList.contains(processTaskVo.getReporter())) {
                        currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
                    }
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(processTaskVo.getId());
                    processTaskStepUserVo.setUserUuidList(userUuidList);
                    processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
                    if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                        currentUserProcessUserTypeList.add(ProcessUserType.MAJOR.getValue());
                    }
                    processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                    if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                        currentUserProcessUserTypeList.add(ProcessUserType.MINOR.getValue());
                    }
                    if (processTaskMapper.checkIsWorker(processTaskVo.getId(), null, null, authenticationInfoVo) > 0) {
                        currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                    }
                } else {
                    // 没有工单id说明是在上报页，当用户即是上报人、代报人、处理人、协助处理人、待处理人
                    currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.MAJOR.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.MINOR.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                }

                List<String> teamUuidList = authenticationInfoVo.getTeamUuidList();
                List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
                for (int i = 0; i < controllerList.size(); i++) {
                    JSONObject attributeObj = controllerList.getJSONObject(i);
                    String uuid = attributeObj.getString("uuid");
                    JSONObject config = attributeObj.getJSONObject("config");
                    if (MapUtils.isNotEmpty(config)) {
                        boolean isHide = true;
                        JSONArray authorityArray = config.getJSONArray("authorityConfig");
                        if (CollectionUtils.isNotEmpty(authorityArray)) {
                            List<String> authorityList = authorityArray.toJavaList(String.class);
                            for (String authority : authorityList) {
                                String[] split = authority.split("#");
                                if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                    if (currentUserProcessUserTypeList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                                    if (currentUserProcessUserTypeList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                    if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                    if (teamUuidList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                    if (roleUuidList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isHide) {
                            resultList.add(uuid);
                        }
                    }
                }
            }
        }
        return resultList;
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
    }

    @Override
    public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId) {
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

    @Override
    public List<String> getProcessUserTypeList(Long processTaskId, AuthenticationInfoVo authenticationInfoVo) {
        List<String> processUserTypeList = new ArrayList<>();
        if (processTaskId != null) {
            String userUuid = authenticationInfoVo.getUserUuid();
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (userUuid.equals(processTaskVo.getOwner())) {
                processUserTypeList.add(ProcessUserType.OWNER.getValue());
            }
            if (userUuid.equals(processTaskVo.getReporter())) {
                processUserTypeList.add(ProcessUserType.REPORTER.getValue());
            }
            List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserList(new ProcessTaskStepUserVo(processTaskId, null, userUuid));
            for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                if (processTaskStepUserVo.getUserType().equals(ProcessUserType.MAJOR.getValue())) {
                    processUserTypeList.add(ProcessUserType.MAJOR.getValue());
                } else {
                    processUserTypeList.add(ProcessUserType.MINOR.getValue());
                }
            }
            if (processUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {
                processUserTypeList.add(ProcessUserType.WORKER.getValue());
            } else {
                if (processTaskMapper.checkIsWorker(processTaskVo.getId(), null, ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0) {
                    processUserTypeList.add(ProcessUserType.WORKER.getValue());
                }
            }
        } else {
            processUserTypeList.add(ProcessUserType.OWNER.getValue());
            processUserTypeList.add(ProcessUserType.REPORTER.getValue());
            processUserTypeList.add(ProcessUserType.MAJOR.getValue());
        }
        return processUserTypeList;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId)
            throws Exception {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        if (processTaskVo.getIsShow() != 1 && !AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName())) {
            throw new PermissionDeniedException(PROCESSTASK_MODIFY.class);
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
    public List<AssignableWorkerStepVo> getAssignableWorkerStepList(Long processTaskId, String processStepUuid) {
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList =
                processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if (CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            List<AssignableWorkerStepVo> assignableWorkerStepList = new ArrayList<>();
            for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
                if (WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    JSONObject configObj = workerPolicyVo.getConfigObj();
                    if (MapUtils.isNotEmpty(configObj)) {
                        JSONArray processStepUuidList = configObj.getJSONArray("processStepUuidList");
                        if (CollectionUtils.isNotEmpty(processStepUuidList)) {
                            for (String stepUuid : processStepUuidList.toJavaList(String.class)) {
                                if (processStepUuid.equals(stepUuid)) {
                                    List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(
                                            workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                                    if (CollectionUtils.isEmpty(majorList)) {
                                        ProcessTaskStepVo processTaskStepVo = processTaskMapper
                                                .getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                                        AssignableWorkerStepVo assignableWorkerStepVo = new AssignableWorkerStepVo();
                                        assignableWorkerStepVo.setId(processTaskStepVo.getId());
                                        assignableWorkerStepVo.setProcessStepUuid(processTaskStepVo.getProcessStepUuid());
                                        assignableWorkerStepVo.setName(processTaskStepVo.getName());
                                        assignableWorkerStepVo.setIsRequired(configObj.getInteger("isRequired"));
                                        assignableWorkerStepVo.setGroupList(configObj.getJSONArray("groupList"));
                                        assignableWorkerStepVo.setRangeList(configObj.getJSONArray("rangeList"));
                                        assignableWorkerStepList.add(assignableWorkerStepVo);
                                    }
                                }
                            }
                        }
                    }
                    List<String> processStepUuidList =
                            JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);

                }
            }
            return assignableWorkerStepList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<AssignableWorkerStepVo> getAssignableWorkerStepList(String processUuid, String processStepUuid) {
        List<ProcessStepWorkerPolicyVo> processStepWorkerPolicyList =
                processMapper.getProcessStepWorkerPolicyListByProcessUuid(processUuid);
        if (CollectionUtils.isNotEmpty(processStepWorkerPolicyList)) {
            List<AssignableWorkerStepVo> assignableWorkerStepList = new ArrayList<>();
            for (ProcessStepWorkerPolicyVo workerPolicyVo : processStepWorkerPolicyList) {
                if (WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    JSONObject configObj = workerPolicyVo.getConfigObj();
                    if (MapUtils.isNotEmpty(configObj)) {
                        JSONArray processStepUuidList = configObj.getJSONArray("processStepUuidList");
                        if (CollectionUtils.isNotEmpty(processStepUuidList)) {
                            for (String stepUuid : processStepUuidList.toJavaList(String.class)) {
                                if (processStepUuid.equals(stepUuid)) {
                                    ProcessStepVo processStep = processMapper.getProcessStepByUuid(workerPolicyVo.getProcessStepUuid());
                                    AssignableWorkerStepVo assignableWorkerStepVo = new AssignableWorkerStepVo();
                                    assignableWorkerStepVo.setProcessStepUuid(processStep.getUuid());
                                    assignableWorkerStepVo.setName(processStep.getName());
                                    assignableWorkerStepVo.setIsRequired(configObj.getInteger("isRequired"));
                                    assignableWorkerStepVo.setGroupList(configObj.getJSONArray("groupList"));
                                    assignableWorkerStepVo.setRangeList(configObj.getJSONArray("rangeList"));
                                    assignableWorkerStepList.add(assignableWorkerStepVo);
                                }
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
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepId(Long processTaskStepId) {
        List<Long> slaIdList = processTaskSlaMapper.getSlaIdListByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(slaIdList)) {
            return processTaskSlaMapper.getProcessTaskSlaTimeListBySlaIdList(slaIdList);
        }
        return new ArrayList<>();
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
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.PROCESSTASK_START.getValue());
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
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.PROCESSTASK_START.getValue());
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
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.PROCESSTASK_START.getValue());
                    processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                    oldContentId = processTaskStepContentVo.getId();
                }
                ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
                processTaskStepFileVo.setProcessTaskId(processTaskId);
                processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
                processTaskStepFileVo.setContentId(oldContentId);
                for (Long fileId : fileIdList) {
                    if (fileMapper.getFileById(fileId) == null) {
                        throw new FileNotFoundException(fileId);
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        } else if (CollectionUtils.isNotEmpty(fileIdList)) {
            isUpdate = true;
            if (oldContentId == null) {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.PROCESSTASK_START.getValue());
                processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                oldContentId = processTaskStepContentVo.getId();
            }
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(processTaskId);
            processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
            processTaskStepFileVo.setContentId(oldContentId);
            for (Long fileId : fileIdList) {
                if (fileMapper.getFileById(fileId) == null) {
                    throw new FileNotFoundException(fileId);
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
        JSONArray authorityList = null;
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        Integer enableAuthority = (Integer) JSONPath.read(stepConfig, "enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = (JSONArray) JSONPath.read(stepConfig, "authorityList");
        } else {
            String handler = processTaskStepVo.getHandler();
            IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(handler);
            }
            String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
            JSONObject globalConfig = null;
            if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                globalConfig = JSONObject.parseObject(processStepHandlerConfig);
            }
            globalConfig = processStepUtilHandler.makeupConfig(globalConfig);
            authorityList = globalConfig.getJSONArray("authorityList");
        }

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
                    AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
                    List<String> teamUuidList = authenticationInfoVo.getTeamUuidList();
                    List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(processTaskId);
                    processTaskStepUserVo.setProcessTaskStepId(processTaskStepId);
//                    processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                    processTaskStepUserVo.setUserUuid(userUuid);
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
//        List<Long> fromStepIdList = processTaskMapper.getFromProcessTaskStepIdListByToId(processTaskStepId);
        List<Long> fromStepIdList = new ArrayList<>();
        List<ProcessTaskStepRelVo> fromProcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByToId(processTaskStepId);
        for (ProcessTaskStepRelVo relVo : fromProcessTaskStepRelList) {
            if (Objects.equals(relVo.getIsHit(), 1) && Objects.equals(relVo.getType(), ProcessFlowDirection.FORWARD.getValue())) {
                fromStepIdList.add(relVo.getFromProcessTaskStepId());
            }
        }
        if (CollectionUtils.isNotEmpty(fromStepIdList)) {
            List<ProcessTaskStepVo> fromStepList = processTaskMapper.getProcessTaskStepListByIdList(fromStepIdList);
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
        if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_URGE, userUuid)) {
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
                if (StringUtils.isNotBlank(content)) {
                    /** 有图片标签才显式点击详情 **/
                    if (content.contains("<figure class=\"image\">") && content.contains("</figure>")) {
                        processTaskStepRemindVo.setDetail(content);
                    }
                    processTaskStepRemindVo.setContent(pattern_html.matcher(content).replaceAll(""));
                }
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
        if (processTaskVo == null){
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }

        // 判断当前用户是否关注该工单
        if (processTaskMapper.checkProcessTaskFocusExists(processTaskId, UserContext.get().getUserUuid()) > 0) {
            processTaskVo.setIsFocus(1);
        }

        // 优先级
        String taskConfigStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
        if (StringUtils.isNotBlank(taskConfigStr)) {
            JSONObject taskConfig = JSONObject.parseObject(taskConfigStr);
            if (MapUtils.isNotEmpty(taskConfig) && (!taskConfig.containsKey("isNeedPriority") || Objects.equals(taskConfig.getInteger("isNeedPriority"), 1))) {
                PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
                if (priorityVo == null) {
                    priorityVo = new PriorityVo();
                    priorityVo.setUuid(processTaskVo.getPriorityUuid());
                }
                processTaskVo.setPriority(priorityVo);
                processTaskVo.setIsNeedPriority(1);
            }else{
                processTaskVo.setIsNeedPriority(0);
            }
        }
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
        setProcessTaskFormInfo(processTaskVo);

        /** 上报人公司、部门列表 **/
        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTaskVo.getOwner());
        List<TeamVo> teamList = null;
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            Set<Long> idSet = new HashSet<>();
            teamList = teamMapper.getTeamByUuidList(teamUuidList);
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
                List<TeamVo> departmentList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(),
                        TeamLevel.DEPARTMENT.getValue());
                if (CollectionUtils.isNotEmpty(departmentList)) {
                    for (TeamVo team : departmentList) {
                        if (!idSet.contains(team.getId())) {
                            idSet.add(team.getId());
                            processTaskVo.getOwnerDepartmentList().add(team);
                        }
                    }
                }
            }
        }
        /** 获取评分信息 */
        String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(processTaskId);
        processTaskVo.setScoreInfo(scoreInfo);

        /** 转报数据 **/
//        Long fromProcessTaskId = processTaskMapper.getFromProcessTaskIdByToProcessTaskId(processTaskId);
        ProcessTaskTranferReportVo processTaskTranferReportVo = processTaskMapper.getProcessTaskTransferReportByToProcessTaskId(processTaskId);
        if (processTaskTranferReportVo != null) {
            ProcessTaskVo fromProcessTaskVo = getFromProcessTaskById(processTaskTranferReportVo.getFromProcessTaskId());
            ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationById(processTaskTranferReportVo.getChannelTypeRelationId());
            if (channelTypeRelationVo != null) {
                fromProcessTaskVo.setChannelTypeRelationName(channelTypeRelationVo.getName());
            }
            processTaskVo.getTranferReportProcessTaskList().add(fromProcessTaskVo);
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
                .TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE).build()
                .check() ? 1 : 0;
        processTaskVo.setCanEditFocusUser(canEditFocusUser);

        String owner = processTaskVo.getOwner();
        UserVo ownerVo = userMapper.getUserBaseInfoByUuid(owner);
        if (ownerVo != null) {
            ownerVo.setTeamList(teamList);
            processTaskVo.setOwnerVo(ownerVo);
        }

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
            if (ProcessTaskOperationType.PROCESSTASK_START.getValue().equals(processTaskStepContent.getType())) {
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
        startProcessTaskStepVo.setReplaceableTextList(getReplaceableTextList(startProcessTaskStepVo));
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
        processTaskStepVo.getParamObj().putAll(currentProcessTaskStep.getParamObj());
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
//        processTaskStepVo.setCurrentSubtaskVo(currentProcessTaskStep.getCurrentSubtaskVo());
        processTaskStepVo.setProcessTaskStepTaskVo(currentProcessTaskStep.getProcessTaskStepTaskVo());
        return processTaskStepVo;
    }

    @Override
    public ProcessTaskVo getFromProcessTaskById(Long processTaskId) {
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
            setProcessTaskFormInfo(processTaskVo);
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
                    .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserUuid()));
        }
        /** 子任务处理人 **/
        processTaskStepUser.setUserType(ProcessUserType.MINOR.getValue());
        List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
        for (ProcessTaskStepUserVo processTaskStepUserVo : minorUserList) {
            receiverMap.computeIfAbsent(ProcessUserType.MINOR.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserUuid()));
        }
        /* 任务处理人 */
        ProcessTaskStepTaskVo stepTaskVo = currentProcessTaskStepVo.getProcessTaskStepTaskVo();
        if (stepTaskVo != null) {
            List<ProcessTaskStepTaskUserVo> taskUserVoList = stepTaskVo.getStepTaskUserVoList();
            if (CollectionUtils.isNotEmpty(taskUserVoList)) {
                for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
                    receiverMap.computeIfAbsent(stepTaskVo.getTaskConfigId().toString(), k -> new ArrayList<>())
                            .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), taskUserVo.getUserUuid()));
                }
            }
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
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(currentProcessTaskStepVo.getConfigHash());
        if (StringUtils.isNotBlank(stepConfig)) {
            String defaultWorker = (String) JSONPath.read(stepConfig, "workerPolicyConfig.defaultWorker");
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
                if (tagList != null) {
                    processTaskVo.setTagList(tagList);
                }
                /** 工单关注人列表 **/
                List<String> focusUserUuidList = JSON.parseArray(dataObj.getString("focusUserUuidList"), String.class);
                if (CollectionUtils.isNotEmpty(focusUserUuidList)) {
                    processTaskVo.setFocusUserUuidList(focusUserUuidList);
                }
            }
        }
    }


    /**
     * 查询待处理的工单，构造"用户uuid->List<工单字段中文名->值>"的map集合
     *
     * @param conditionMap 工单查询条件
     * @return "用户uuid->List<工单字段中文名->值>"的map集合
     */
    @Override
    public Map<String, List<Map<String, Object>>> getProcessingUserTaskMapByCondition(Map<String, Object> conditionMap) {

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
     *
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
     *
     * @param conditionMap 查询条件
     * @param user         用户
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

    @Override
    public JSONArray getReplaceableTextList(ProcessTaskStepVo processTaskStepVo) {
        String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        boolean stepLevelTakesEffect = false;
        JSONArray replaceableTextList = (JSONArray) JSONPath.read(config, "replaceableTextList");
        if (CollectionUtils.isNotEmpty(replaceableTextList)) {
            for (int i = 0; i < replaceableTextList.size(); i++) {
                JSONObject replaceableText = replaceableTextList.getJSONObject(i);
                if (StringUtils.isNotBlank(replaceableText.getString("value"))) {
                    stepLevelTakesEffect = true;
                    break;
                }
            }
        }
        if (!stepLevelTakesEffect) {
            String handler = processTaskStepVo.getHandler();
            if (StringUtils.isNotBlank(handler)) {
                String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
                if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                    JSONObject configObj = JSONObject.parseObject(processStepHandlerConfig);
                    if (MapUtils.isNotEmpty(configObj)) {
                        replaceableTextList = configObj.getJSONArray("replaceableTextList");
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(replaceableTextList)) {
            replaceableTextList = new JSONArray();
            for (ReplaceableText replaceableText : ReplaceableText.values()) {
                replaceableTextList.add(new JSONObject() {{
                    this.put("name", replaceableText.getValue());
                    this.put("text", replaceableText.getText());
                    this.put("value", "");
                }});
            }
        }
        return replaceableTextList;
    }

    /**
     * 刷新minor worker
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    @Override
    public void refreshStepMinorWorker(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo) {
        //删除该step的所有minor工单步骤worker
        processTaskMapper.deleteProcessTaskStepWorkerMinorByProcessTaskStepId(processTaskStepVo.getId());
        List<ProcessTaskStepWorkerVo> workerVoList = new ArrayList<>();
        //重新更新每个模块的minor worker
        for (IProcessStepHandler handler : ProcessStepHandlerFactory.getHandlerList()) {
            workerVoList.addAll(handler.getMinorWorkerList(processTaskStepVo));

        }
        //重新插入pending任务用户到 工单步骤worker
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getStepTaskUserListByProcessTaskStepId(processTaskStepVo.getId());
        for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
            if (taskUserVo.getIsDelete() != 1 && Objects.equals(ProcessTaskStatus.PENDING.getValue(), taskUserVo.getStatus())) {
                workerVoList.add(new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), GroupSearch.USER.getValue(), taskUserVo.getUserUuid(), ProcessUserType.MINOR.getValue()));
            }
        }

        for (ProcessTaskStepWorkerVo workerVo : workerVoList) {
            processTaskMapper.insertIgnoreProcessTaskStepWorker(workerVo);
        }
    }

    /**
     * 刷新minor user
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    @Override
    public void refreshStepMinorUser(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo) {
        List<ProcessTaskStepUserVo> stepUserVoList = new ArrayList<>();
        //如果存在子任务已完成的用户,且该用户为succeed， 则更新到 processtask_step_user ，且status 为 done，type 为minor
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getStepTaskUserListByProcessTaskStepId(processTaskStepVo.getId());
        taskUserVoList = taskUserVoList.stream().filter(user->Objects.equals(ProcessTaskStatus.SUCCEED.getValue(),user.getStatus())).collect(collectingAndThen(toCollection(() -> new TreeSet<>( Comparator.comparing(ProcessTaskStepTaskUserVo::getUserUuid))), ArrayList::new));
        for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
            if (taskUserVo.getIsDelete() != 1) {
                String status = ProcessTaskStepUserStatus.DOING.getValue();
                if (Objects.equals(taskUserVo.getStatus(), ProcessTaskStatus.SUCCEED.getValue())) {
                    status = ProcessTaskStepUserStatus.DONE.getValue();
                }
                UserVo userVo = userMapper.getUserBaseInfoByUuid(taskUserVo.getUserUuid());
                if (userVo != null) {
                    stepUserVoList.add(new ProcessTaskStepUserVo(status, processTaskStepTaskVo.getCreateTime(), taskUserVo.getEndTime(), processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userVo.getUuid(), userVo.getUserName(), ProcessUserType.MINOR.getValue()));
                }
            }
        }
        //TODO 其它模块的taskStepUser
        //delete该step的所有minor工单步骤user
        processTaskMapper.deleteProcessTaskStepUserMinorByProcessTaskStepId(processTaskStepVo.getId());
        //insert该step的所有minor工单步骤user
        for (ProcessTaskStepUserVo stepUserVo : stepUserVoList) {
            processTaskMapper.insertIgnoreProcessTaskStepUser(stepUserVo);
        }
    }
}
