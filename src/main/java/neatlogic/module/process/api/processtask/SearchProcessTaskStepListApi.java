package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.notify.core.NotifyTriggerTypeFactory;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.*;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchProcessTaskStepListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private ProcessTaskStepTaskService processTaskStepTaskService;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private ProcessTaskActionMapper processTaskActionMapper;
    @Resource
    private IntegrationMapper integrationMapper;
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    TaskMapper taskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Override
    public String getToken() {
        return "processtask/step/list";
    }

    @Override
    public String getName() {
        return "工单步骤列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid")
    })
    @Output({
            @Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "工单步骤列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<Long> processTaskStepIdList = new ArrayList<>();
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            if (Objects.equals(processTaskStepVo.getType(), ProcessStepType.START.getValue())) {
                resultList.add(processTaskStepVo);
                processTaskStepIdList.add(processTaskStepVo.getId());
            } else if (Objects.equals(processTaskStepVo.getType(), ProcessStepType.PROCESS.getValue())) {
                if (processTaskStepVo.getActiveTime() != null) {
                    resultList.add(processTaskStepVo);
                    processTaskStepIdList.add(processTaskStepVo.getId());
                }
            }
        }
        ProcessAuthManager build = new ProcessAuthManager.Builder()
                .addProcessTaskId(processTaskId)
                .addOperationType(ProcessTaskOperationType.PROCESSTASK_VIEW)
                .addProcessTaskStepId(processTaskStepIdList)
                .addOperationType(ProcessTaskStepOperationType.STEP_VIEW)
                .build();
        Map<Long, Set<IOperationType>> operateMap = build.getOperateMap();
        Set<IOperationType> operationTypeList = operateMap.get(processTaskId);
        if (CollectionUtils.isEmpty(operationTypeList) || !operationTypeList.contains(ProcessTaskOperationType.PROCESSTASK_VIEW)) {
            ProcessTaskPermissionDeniedException processTaskPermissionDeniedException = build.getProcessTaskPermissionDeniedException(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW);
            if (processTaskPermissionDeniedException != null) {
                throw new PermissionDeniedException(processTaskPermissionDeniedException.getMessage());
            }
        }

        // 其他处理步骤
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (ProcessTaskStepVo processTaskStepVo : resultList) {
                // 判断当前用户是否有权限查看该节点信息
                Set<IOperationType> processTaskStepOperateSet = operateMap.get(processTaskStepVo.getId());
                if (CollectionUtils.isNotEmpty(processTaskStepOperateSet)
                        && processTaskStepOperateSet.contains(ProcessTaskStepOperationType.STEP_VIEW)) {
                    processTaskStepVo.setIsView(1);
                } else if (Objects.equals(processTaskStepVo.getType(), ProcessStepType.START.getValue())) {
                    processTaskStepVo.setIsView(1);
                } else {
                    processTaskStepVo.setIsView(0);
                }
            }
            getProcessTaskStepDetailList(processTaskVo, resultList);
            for (ProcessTaskStepVo processTaskStepVo : resultList) {
                processTaskStepVo.setIsInTheCurrentStepTab(0);
                if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                    if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.PENDING.getValue())
                            || Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())
                            || Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.HANG.getValue())) {
                        processTaskStepVo.setIsInTheCurrentStepTab(1);
                    }
                }
            }
            resultList.sort((o1, o2) -> {
                if (o1.getActiveTime() != null && o2.getActiveTime() != null) {
                    int i = o1.getActiveTime().compareTo(o2.getActiveTime());
                    if (i == 0) {
                        return o1.getId().compareTo(o2.getId());
                    } else {
                        return i;
                    }
                } else {
                    return -1;
                }
            });
        }
        return resultList;
    }

    private void getProcessTaskStepDetailList(ProcessTaskVo processTaskVo, List<ProcessTaskStepVo> processTaskStepList) {
        Long processTaskId = processTaskVo.getId();
        List<Long> processTaskStepIdList = processTaskStepList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
        ProcessTaskStepUserVo searchStepUserVo = new ProcessTaskStepUserVo();
        searchStepUserVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserList(searchStepUserVo);
        List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskId, null);
        {
            Set<String> userUuidSet = new HashSet<>();
            Set<String> teamUuidSet = new HashSet<>();
            Set<String> roleUuidSet = new HashSet<>();
            for (ProcessTaskStepUserVo stepUserVo : processTaskStepUserList) {
                userUuidSet.add(stepUserVo.getUserUuid());
            }
            for (ProcessTaskStepWorkerVo workerVo : processTaskStepWorkerList) {
                if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                    userUuidSet.add(workerVo.getUuid());
                } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                    teamUuidSet.add(workerVo.getUuid());
                } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                    roleUuidSet.add(workerVo.getUuid());
                }
            }
            Map<String, UserVo> userMap = new HashMap<>();
            Map<String, TeamVo> teamMap = new HashMap<>();
            Map<String, RoleVo> roleMap = new HashMap<>();
            List<String> userUuidList = userUuidSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
                userMap = userList.stream().collect(Collectors.toMap(UserVo::getUuid, e -> e));
            }
            List<String> teamUuidList = teamUuidSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                List<TeamVo> teamList = teamMapper.getTeamListContainsDeletedByUuidList(teamUuidList);
                teamMap = teamList.stream().collect(Collectors.toMap(TeamVo::getUuid, e -> e));
            }
            List<String> roleUuidList = roleUuidSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(roleUuidList)) {
                List<RoleVo> roleList = roleMapper.getRoleListContainsDeletedByUuidList(roleUuidList);
                roleMap = roleList.stream().collect(Collectors.toMap(RoleVo::getUuid, e -> e));
            }
            for (ProcessTaskStepUserVo stepUserVo : processTaskStepUserList) {
                UserVo userVo = userMap.get(stepUserVo.getUserUuid());
                if (userVo != null) {
                    stepUserVo.setUserName(userVo.getUserName());
                }
            }
            for (ProcessTaskStepWorkerVo workerVo : processTaskStepWorkerList) {
                if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                    UserVo userVo = userMap.get(workerVo.getUuid());
                    if (userVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                        workerVo.setName(userVo.getUserName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                    TeamVo teamVo = teamMap.get(workerVo.getUuid());
                    if (teamVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                        workerVo.setName(teamVo.getName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                    RoleVo roleVo = roleMap.get(workerVo.getUuid());
                    if (roleVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                        workerVo.setName(roleVo.getName());
                    }
                }
            }
        }
        ProcessTaskStepDataVo searchStepDataVo = new ProcessTaskStepDataVo();
        searchStepDataVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepDataVo> processTaskStepDataList = processTaskStepDataMapper.searchProcessTaskStepData(searchStepDataVo);
        List<String> focusUserList = processTaskMapper.getFocusUserListByTaskId(processTaskId);
        {
            Map<String, String> hash2ConfigMap = new HashMap<>();
            List<String> configHashList = processTaskStepList.stream().map(ProcessTaskStepVo::getConfigHash).filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(configHashList)) {
                List<ProcessTaskStepConfigVo> configList = selectContentByHashMapper.getProcessTaskStepConfigListByHashList(configHashList);
                for (ProcessTaskStepConfigVo processTaskStepConfigVo : configList) {
                    hash2ConfigMap.put(processTaskStepConfigVo.getHash(), processTaskStepConfigVo.getConfig());
                }
            }
            for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                String stepConfig = hash2ConfigMap.get(processTaskStepVo.getConfigHash());
                if (StringUtils.isNotBlank(stepConfig)) {
                    processTaskStepVo.setConfig(JSON.parseObject(stepConfig));
                } else {
                    processTaskStepVo.setConfig(new JSONObject());
                }
            }
        }
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = getProcessTaskStepReplyListByProcessTaskId(processTaskVo, processTaskStepList, processTaskStepUserList, processTaskStepWorkerList, focusUserList);
        List<ProcessTaskActionVo> processTaskActionList = getProcessTaskActionListByProcessTaskId(processTaskVo.getId());
        Map<Long, List<Long>> processTaskStepId2SlaIdListMap = new HashMap<>();
        Set<Long> slaIdSet = new HashSet<>();
        List<Map<String, Long>> processTaskStepSlaList = processTaskSlaMapper.getProcessTaskStepSlaListByProcessTaskStepIdList(processTaskStepIdList);
        for (Map<String, Long> processTaskStepSlaMap : processTaskStepSlaList) {
            Long processTaskStepId = processTaskStepSlaMap.get("processTaskStepId");
            Long slaId = processTaskStepSlaMap.get("slaId");
            processTaskStepId2SlaIdListMap.computeIfAbsent(processTaskStepId, key -> new ArrayList<>()).add(slaId);
            slaIdSet.add(slaId);
        }
        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskService.getSlaTimeListBySlaIdList(new ArrayList<>(slaIdSet));
        List<ProcessTaskStepTaskVo> processTaskStepTaskList = getProcessTaskStepTaskListByProcessTaskId(processTaskVo, processTaskStepList);
        List<TaskConfigVo> allTaskConfigList = getAllTaskConfigList(processTaskStepList);
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            if (Objects.equals(processTaskStepVo.getIsView(), 1)) {
                // 处理人列表
                processTaskService.setProcessTaskStepUser(processTaskStepVo, processTaskStepUserList, processTaskStepWorkerList);
                // 当前步骤特有步骤信息
                IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
                if (processStepUtilHandler != null) {
                    if (Objects.equals(processTaskStepVo.getType(), ProcessStepType.START.getValue())) {
                        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getStartStepInfo(processTaskStepVo));
                    } else {
                        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getNonStartStepInfo(processTaskStepVo));
                    }
                }
                // 步骤评论列表
                List<ProcessTaskStepReplyVo> commentList = new ArrayList<>();
                for (ProcessTaskStepReplyVo processTaskStepReplyVo : processTaskStepReplyList) {
                    if (Objects.equals(processTaskStepReplyVo.getProcessTaskStepId(), processTaskStepVo.getId())) {
                        commentList.add(processTaskStepReplyVo);
                    }
                }
                commentList.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
                processTaskStepVo.setCommentList(commentList);
                if (Objects.equals(processTaskStepVo.getType(), ProcessStepType.START.getValue())) {
                    for (ProcessTaskStepReplyVo comment : commentList) {
                        if (ProcessTaskOperationType.PROCESSTASK_START.getValue().equals(comment.getType())) {
                            processTaskStepVo.setComment(comment);
                            break;
                        }
                    }
                }
                // 动作列表
                List<ProcessTaskActionVo> actionList = new ArrayList<>();
                for (ProcessTaskActionVo processTaskActionVo : processTaskActionList) {
                    if (Objects.equals(processTaskActionVo.getProcessTaskStepId(), processTaskStepVo.getId())) {
                        actionList.add(processTaskActionVo);
                    }
                }
                processTaskStepVo.setActionList(actionList);
                //任务列表
                List<TaskConfigVo> taskConfigList = getTaskConfigList(processTaskStepVo, processTaskStepTaskList, allTaskConfigList);
                processTaskStepVo.setTaskConfigList(taskConfigList);
                // 时效列表
                List<ProcessTaskSlaTimeVo> slaTimeList = new ArrayList<>();
                List<Long> slaIdList = processTaskStepId2SlaIdListMap.get(processTaskStepVo.getId());
                if (CollectionUtils.isNotEmpty(slaIdList)) {
                    for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
                        if (slaIdList.contains(processTaskSlaTimeVo.getSlaId())) {
                            slaTimeList.add(processTaskSlaTimeVo);
                        }
                    }
                }
                processTaskStepVo.setSlaTimeList(slaTimeList);
                // automatic processtaskStepData
                ProcessTaskStepDataVo stepDataVo = null;
                for (ProcessTaskStepDataVo processTaskStepDataVo : processTaskStepDataList) {
                    if (Objects.equals(processTaskStepDataVo.getProcessTaskStepId(), processTaskStepVo.getId())
                            && Objects.equals(processTaskStepDataVo.getType(), processTaskStepVo.getHandler())
                            && processTaskStepDataVo.getFcu() != null && Objects.equals(processTaskStepDataVo.getFcu().toLowerCase(), SystemUser.SYSTEM.getUserUuid())
                    ) {
                        stepDataVo = processTaskStepDataVo;
                        break;
                    }
                }
                if (stepDataVo != null) {
                    JSONObject stepDataJson = stepDataVo.getData();
                    boolean isStepUser = false;
                    for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                        if (Objects.equals(processTaskStepUserVo.getProcessTaskStepId(), processTaskStepVo.getId())
                                && Objects.equals(processTaskStepUserVo.getUserUuid(), UserContext.get().getUserUuid())
                        ) {
                            isStepUser = true;
                        }
                    }
                    stepDataJson.put("isStepUser", isStepUser ? 1 : 0);
                    processTaskStepVo.setProcessTaskStepData(stepDataJson);
                }
                processTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(processTaskStepVo));
                processTaskStepVo.setCustomStatusList(processTaskService.getCustomStatusList(processTaskStepVo));
                processTaskStepVo.setCustomButtonList(processTaskService.getCustomButtonList(processTaskStepVo));
            }
            processTaskStepVo.setConfig(null);
        }
    }


    private List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskId(
            ProcessTaskVo processTaskVo,
            List<ProcessTaskStepVo> processTaskStepList,
            List<ProcessTaskStepUserVo> processTaskStepUserList,
            List<ProcessTaskStepWorkerVo> processTaskStepWorkerList,
            List<String> focusUserList
    ) {
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskStepOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskStepOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskStepOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_TRANSFER.getValue());
        typeList.add(ProcessTaskStepOperationType.STEP_REAPPROVAL.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_START.getValue());
        typeList.add(ProcessTaskStepOperationType.STEP_TRANSFER.getValue());
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
        {
            List<Long> stepContentIdList = new ArrayList<>();
            Set<String> contentHashSet = new HashSet<>();
            List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskId(processTaskVo.getId());
            for (ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
                stepContentIdList.add(processTaskStepContentVo.getId());
                if (processTaskStepContentVo.getContentHash() != null) {
                    contentHashSet.add(processTaskStepContentVo.getContentHash());
                }
            }
            Map<Long, FileVo> fileMap = new HashMap<>();
            List<ProcessTaskStepFileVo> processTaskStepFileList = processTaskMapper.getProcessTaskStepFileListByTaskId(processTaskVo.getId());
            Set<Long> fileIdSet = processTaskStepFileList.stream().map(ProcessTaskStepFileVo::getFileId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(fileIdSet)) {
                List<FileVo> fileList = fileMapper.getFileListByIdList(new ArrayList<>(fileIdSet));
                fileMap = fileList.stream().collect(Collectors.toMap(FileVo::getId, e -> e));
            }
            List<ProcessTaskStepContentTargetVo> processTaskStepContentTargetList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(stepContentIdList)) {
                processTaskStepContentTargetList = processTaskMapper.getTargetListByContentIdList(stepContentIdList);
            }
            Map<String, String> hash2ContentMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(contentHashSet)) {
                List<ProcessTaskContentVo> processTaskContentList = selectContentByHashMapper.getProcessTaskContentListByHashList(new ArrayList<>(contentHashSet));
                hash2ContentMap = processTaskContentList.stream().collect(Collectors.toMap(ProcessTaskContentVo::getHash, ProcessTaskContentVo::getContent));
            }
            for (ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
                if (typeList.contains(processTaskStepContentVo.getType())) {
                    ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
                    if (processTaskStepReplyVo.getContentHash() != null) {
                        processTaskStepReplyVo.setContent(hash2ContentMap.get(processTaskStepReplyVo.getContentHash()));
                    }
                    List<Long> fileIdList = processTaskStepFileList.stream().filter(processTaskStepFileVo -> Objects.equals(processTaskStepFileVo.getContentId(), processTaskStepReplyVo.getId())).map(ProcessTaskStepFileVo::getFileId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fileIdList)) {
                        List<FileVo> fileList = new ArrayList<>();
                        for (Long fileId : fileIdList) {
                            FileVo fileVo = fileMap.get(fileId);
                            if (fileVo != null) {
                                fileList.add(fileVo);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(fileList)) {
                            processTaskStepReplyVo.setFileList(fileList);
                            processTaskStepReplyVo.setFileIdList(fileList.stream().map(FileVo::getId).collect(Collectors.toList()));
                        }
                    }
                    List<WorkAssignmentUnitVo> targetList = new ArrayList<>();
                    for (ProcessTaskStepContentTargetVo processTaskStepContentTargetVo : processTaskStepContentTargetList) {
                        if (Objects.equals(processTaskStepContentTargetVo.getContentId(), processTaskStepReplyVo.getId())) {
                            WorkAssignmentUnitVo workAssignmentUnitVo = new WorkAssignmentUnitVo();
                            workAssignmentUnitVo.setInitType(processTaskStepContentTargetVo.getType());
                            workAssignmentUnitVo.setUuid(processTaskStepContentTargetVo.getUuid());
                            targetList.add(workAssignmentUnitVo);
                        }
                    }
                    processTaskStepReplyVo.setTargetList(targetList);
                    processTaskStepReplyList.add(processTaskStepReplyVo);
                }
            }
        }
        List<ProcessUserType> processUserTypeList = new ArrayList<>();
        processUserTypeList.add(ProcessUserType.OWNER);
        processUserTypeList.add(ProcessUserType.REPORTER);
        processUserTypeList.add(ProcessUserType.MAJOR);
        processUserTypeList.add(ProcessUserType.MINOR);
        processUserTypeList.add(ProcessUserType.FOCUS_USER);
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            Map<ProcessUserType, List<String>> processUserTypeListMap = getProcessTaskStepProcessUserTypeData(processTaskVo, processTaskStepVo, processTaskStepUserList, processTaskStepWorkerList, focusUserList, processUserTypeList);
            for (ProcessTaskStepReplyVo processTaskStepReplyVo : processTaskStepReplyList) {
                if (Objects.equals(processTaskStepReplyVo.getProcessTaskStepId(), processTaskStepVo.getId())) {
                    if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())
                            && Objects.equals(UserContext.get().getUserUuid(), processTaskStepReplyVo.getFcu())) {
                        processTaskStepReplyVo.setIsEditable(1);
                        processTaskStepReplyVo.setIsDeletable(1);
                    } else {
                        processTaskStepReplyVo.setIsEditable(0);
                        processTaskStepReplyVo.setIsDeletable(0);
                    }
                    List<ProcessUserType> operatorProcessUserTypeList = new ArrayList<>();
                    for (Map.Entry<ProcessUserType, List<String>> entry : processUserTypeListMap.entrySet()) {
                        List<String> uuidList = entry.getValue();
                        if (CollectionUtils.isEmpty(uuidList)) {
                            continue;
                        }
                        for (String uuid : uuidList) {
                            if (processTaskStepReplyVo.getLcu() != null && uuid.contains(processTaskStepReplyVo.getLcu())) {
                                operatorProcessUserTypeList.add(entry.getKey());
                                break;
                            }
                        }
                    }
                    List<String> operatorProcessUserTypeTextList = new ArrayList<>(operatorProcessUserTypeList.size());
                    for (ProcessUserType processUserType : operatorProcessUserTypeList) {
                        operatorProcessUserTypeTextList.add(processUserType.getText());
                    }
                    processTaskStepReplyVo.setOperatorRole(String.join("、", operatorProcessUserTypeTextList));
                }
            }
        }
        return processTaskStepReplyList;
    }


    private Map<ProcessUserType, List<String>> getProcessTaskStepProcessUserTypeData(
            ProcessTaskVo processTaskVo,
            ProcessTaskStepVo processTaskStepVo,
            List<ProcessTaskStepUserVo> processTaskStepUserList,
            List<ProcessTaskStepWorkerVo> processTaskStepWorkerList,
            List<String> focusUserList,
            List<ProcessUserType> processUserTypeList
    ) {
        Map<ProcessUserType, List<String>> resultMap = new LinkedHashMap<>();
        if (processTaskVo != null) {
            /* 上报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getOwner()) && processUserTypeList.contains(ProcessUserType.OWNER)) {
                resultMap.computeIfAbsent(ProcessUserType.OWNER, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskVo.getOwner()));
            }
            /* 代报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getReporter()) && processUserTypeList.contains(ProcessUserType.REPORTER)) {
                resultMap.computeIfAbsent(ProcessUserType.REPORTER, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskVo.getReporter()));
            }
        }
        if (processUserTypeList.contains(ProcessUserType.MAJOR)) {
            /* 主处理人 **/
            List<ProcessTaskStepUserVo> majorUserList = new ArrayList<>();
            for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                if (Objects.equals(processTaskStepUserVo.getProcessTaskStepId(), processTaskStepVo.getId())
                        && Objects.equals(processTaskStepUserVo.getUserType(), ProcessUserType.MAJOR.getValue())) {
                    majorUserList.add(processTaskStepUserVo);
                }
            }
            for (ProcessTaskStepUserVo processTaskStepUserVo : majorUserList) {
                resultMap.computeIfAbsent(ProcessUserType.MAJOR, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskStepUserVo.getUserUuid()));
            }
        }
        if (processUserTypeList.contains(ProcessUserType.MINOR)) {
            /* 所有任务处理人 **/
            List<ProcessTaskStepUserVo> minorUserList = new ArrayList<>();
            for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                if (Objects.equals(processTaskStepUserVo.getProcessTaskStepId(), processTaskStepVo.getId())
                        && Objects.equals(processTaskStepUserVo.getUserType(), ProcessUserType.MINOR.getValue())) {
                    minorUserList.add(processTaskStepUserVo);
                }
            }
            for (ProcessTaskStepUserVo processTaskStepUserVo : minorUserList) {
                resultMap.computeIfAbsent(ProcessUserType.MINOR, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskStepUserVo.getUserUuid()));
            }
        }
        if (processUserTypeList.contains(ProcessUserType.WORKER)) {
            /* 待处理人 **/
            for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
                if (Objects.equals(processTaskStepWorkerVo.getProcessTaskStepId(), processTaskStepVo.getId())) {
                    resultMap.computeIfAbsent(ProcessUserType.WORKER, k -> new ArrayList<>()).add(processTaskStepWorkerVo.getType() + "#" + processTaskStepWorkerVo.getUuid());
                }
            }
        }
        if (processUserTypeList.contains(ProcessUserType.FOCUS_USER)) {
            /* 工单关注人 */
            for (String focusUser : focusUserList) {
                resultMap.computeIfAbsent(ProcessUserType.FOCUS_USER, k -> new ArrayList<>())
                        .add(focusUser);
            }
        }
        if (processUserTypeList.contains(ProcessUserType.DEFAULT_WORKER)) {
            JSONObject config = processTaskStepVo.getConfig();
            /* 异常处理人 **/
            String defaultWorker = config.getString("workerPolicyConfig.defaultWorker");
            if (StringUtils.isNotBlank(defaultWorker)) {
                resultMap.computeIfAbsent(ProcessUserType.DEFAULT_WORKER, k -> new ArrayList<>())
                        .add(defaultWorker);
            }
        }
        return resultMap;
    }

    private List<ProcessTaskActionVo> getProcessTaskActionListByProcessTaskId(Long processTaskId) {
        List<ProcessTaskActionVo> processTaskActionList = processTaskActionMapper.getProcessTaskActionListByProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(processTaskActionList)) {
            List<String> integrationUuidList = processTaskActionList.stream().map(ProcessTaskActionVo::getIntegrationUuid).collect(Collectors.toList());
            List<IntegrationVo> integrationList = integrationMapper.getIntegrationListByUuidList(integrationUuidList);
            Map<String, String> uuid2NameMap = integrationList.stream().collect(Collectors.toMap(IntegrationVo::getUuid, IntegrationVo::getName));
            for (ProcessTaskActionVo processTaskActionVo : processTaskActionList) {
                String integrationName = uuid2NameMap.get(processTaskActionVo.getIntegrationUuid());
                if (StringUtils.isNotBlank(integrationName)) {
                    processTaskActionVo.setIntegrationName(integrationName);
                } else {
                    JSONObject config = processTaskActionVo.getConfig();
                    if (MapUtils.isNotEmpty(config)) {
                        integrationName = config.getString("integrationName");
                        if (StringUtils.isNotBlank(integrationName)) {
                            processTaskActionVo.setIntegrationName(integrationName);
                        }
                    }
                }
                if (Objects.equals(processTaskActionVo.getStatus(), "succeed")) {
                    processTaskActionVo.setStatusText("已成功");
                } else {
                    processTaskActionVo.setStatusText("已失败");
                }
                String triggerText = NotifyTriggerTypeFactory.getText(processTaskActionVo.getTrigger());
                if(StringUtils.isNotBlank(triggerText)) {
                    processTaskActionVo.setTriggerText(triggerText);
                }
            }
        }
        return processTaskActionList;
    }

    private List<TaskConfigVo> getAllTaskConfigList(List<ProcessTaskStepVo> processTaskStepList) {
        Set<Long> idSet = new HashSet<>();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            JSONObject config = processTaskStepVo.getConfig();
            JSONObject taskConfig = config.getJSONObject("taskConfig");
            if (MapUtils.isNotEmpty(taskConfig)) {
                JSONArray idArray = taskConfig.getJSONArray("idList");
                if (CollectionUtils.isNotEmpty(idArray)) {
                    for (int i = 0; i < idArray.size(); i++) {
                        Long id = idArray.getLong(i);
                        if (id != null) {
                            idSet.add(id);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(idSet)) {
            return taskMapper.getTaskConfigByIdList(new ArrayList<>(idSet));
        }
        return new ArrayList<>();
    }
    /**
     * 解析&校验 任务配置
     *
     * @param processTaskStepVo 步骤
     * @return 任务配置
     */
    public JSONObject getTaskConfig(ProcessTaskStepVo processTaskStepVo) {
        JSONObject config = processTaskStepVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONObject taskConfig = config.getJSONObject("taskConfig");
            if (MapUtils.isNotEmpty(taskConfig)) {
                return taskConfig;
            }
        }
        return null;
    }

    /**
     * 获取步骤的任务策略列表及其任务列表
     *
     * @param processTaskStepVo 步骤信息
     */

    public List<TaskConfigVo> getTaskConfigList(ProcessTaskStepVo processTaskStepVo, List<ProcessTaskStepTaskVo> processTaskStepTaskList, List<TaskConfigVo> allTaskConfigList) {
        JSONObject taskConfig = getTaskConfig(processTaskStepVo);
        if (MapUtils.isEmpty(taskConfig)) {
            return null;
        }
        JSONArray idArray = taskConfig.getJSONArray("idList");
        if (CollectionUtils.isEmpty(idArray)) {
            return null;
        }
        List<String> rangeList = null;
        JSONArray rangeArray = taskConfig.getJSONArray("rangeList");
        if (CollectionUtils.isNotEmpty(rangeArray)) {
            rangeList = rangeArray.toJavaList(String.class);
        }
        List<TaskConfigVo> taskConfigList = new ArrayList<>();
        List<Long> idList = idArray.toJavaList(Long.class);
        for (Long id : idList) {
            for (TaskConfigVo taskConfigVo : allTaskConfigList) {
                if (Objects.equals(taskConfigVo.getId(), id)) {
                    TaskConfigVo newTaskConfigVo = new TaskConfigVo();
                    newTaskConfigVo.setId(taskConfigVo.getId());
                    newTaskConfigVo.setName(taskConfigVo.getName());
                    newTaskConfigVo.setNum(taskConfigVo.getNum());
                    newTaskConfigVo.setPolicy(taskConfigVo.getPolicy());
                    newTaskConfigVo.setIsActive(taskConfigVo.getIsActive());
                    newTaskConfigVo.setConfig(taskConfigVo.getConfig());
                    newTaskConfigVo.setFcd(taskConfigVo.getFcd());
                    newTaskConfigVo.setFcu(taskConfigVo.getFcu());
                    newTaskConfigVo.setLcd(taskConfigVo.getLcd());
                    newTaskConfigVo.setLcu(taskConfigVo.getLcu());
                    newTaskConfigVo.setRangeList(rangeList);
                    List<ProcessTaskStepTaskVo> stepTaskList = new ArrayList<>();
                    for (ProcessTaskStepTaskVo processTaskStepTaskVo : processTaskStepTaskList) {
                        if (Objects.equals(processTaskStepTaskVo.getProcessTaskStepId(), processTaskStepVo.getId())
                                && Objects.equals(processTaskStepTaskVo.getTaskConfigId(), id)
                        ) {
                            stepTaskList.add(processTaskStepTaskVo);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(stepTaskList)) {
                        newTaskConfigVo.setProcessTaskStepTaskList(stepTaskList);
                    }
                    taskConfigList.add(newTaskConfigVo);
                    break;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(taskConfigList)) {
            return taskConfigList;
        }
        return null;
    }

    private List<ProcessTaskStepTaskVo> getProcessTaskStepTaskListByProcessTaskId(
            ProcessTaskVo processTaskVo,
            List<ProcessTaskStepVo> processTaskStepList
    ) {
        List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskByProcessTaskId(processTaskVo.getId());
        if (CollectionUtils.isEmpty(processTaskStepTaskList)) {
            return processTaskStepTaskList;
        }
        Map<Long, ProcessTaskStepVo> processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getId, e -> e));
        Map<Long, List<ProcessTaskStepTaskUserVo>> stepTaskUserMap = new HashMap<>();
        List<Long> stepTaskIdList = new ArrayList<>();
        Map<Long, Long> stepTaskId2StepIdMap = new HashMap<>();
        for (ProcessTaskStepTaskVo processTaskStepTaskVo : processTaskStepTaskList) {
            stepTaskId2StepIdMap.put(processTaskStepTaskVo.getId(), processTaskStepTaskVo.getProcessTaskStepId());
            stepTaskIdList.add(processTaskStepTaskVo.getId());
        }
        List<ProcessTaskStepTaskUserVo> stepTaskUserList = processTaskStepTaskMapper.getStepTaskUserByStepTaskIdList(stepTaskIdList);
        if (CollectionUtils.isNotEmpty(stepTaskUserList)) {
            List<Long> stepTaskUserIdList = stepTaskUserList.stream().map(ProcessTaskStepTaskUserVo::getId).collect(Collectors.toList());
            List<ProcessTaskStepTaskUserAgentVo> stepTaskUserAgentList = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentListByStepTaskUserIdList(stepTaskUserIdList);
            Map<Long, String> stepTaskUserAgentMap = stepTaskUserAgentList.stream().collect(Collectors.toMap(ProcessTaskStepTaskUserAgentVo::getProcessTaskStepTaskUserId, ProcessTaskStepTaskUserAgentVo::getUserUuid));
            Map<String, UserVo> userMap = new HashMap<>();
            List<String> userUuidList = new ArrayList<>(stepTaskUserAgentMap.values());
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
                userMap = userList.stream().collect(Collectors.toMap(UserVo::getUuid, e -> e));
            }
            List<ProcessTaskStepTaskUserContentVo> stepTaskUserContentList = processTaskStepTaskMapper.getStepTaskUserContentByStepTaskUserIdList(stepTaskUserIdList);
            Map<Long, ProcessTaskStepTaskUserContentVo> stepTaskUserContentMap = new HashMap<>();
            for (ProcessTaskStepTaskUserContentVo stepTaskUserContentVo : stepTaskUserContentList) {
                if (stepTaskUserContentMap.containsKey(stepTaskUserContentVo.getProcessTaskStepTaskUserId())) {
                    continue;
                }
                stepTaskUserContentMap.put(stepTaskUserContentVo.getProcessTaskStepTaskUserId(), stepTaskUserContentVo);
            }
            Map<Long, FileVo> fileMap = new HashMap<>();
            Set<Long> fileIdSet = new HashSet<>();
            Map<Long, List<Long>> stepTaskUserFileIdListMap = new HashMap<>();
            List<ProcessTaskStepTaskUserFileVo> processTaskStepTaskUserFileList = processTaskStepTaskMapper.getStepTaskUserFileListByStepTaskUserIdList(stepTaskUserIdList);
            for (ProcessTaskStepTaskUserFileVo stepTaskUserFileVo : processTaskStepTaskUserFileList) {
                fileIdSet.add(stepTaskUserFileVo.getFileId());
                stepTaskUserFileIdListMap.computeIfAbsent(stepTaskUserFileVo.getProcessTaskStepTaskUserId(), key -> new ArrayList<>()).add(stepTaskUserFileVo.getFileId());
            }
            List<Long> allFileIdList = fileIdSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(allFileIdList)) {
                List<FileVo> allFileList = fileMapper.getFileListByIdList(allFileIdList);
                fileMap = allFileList.stream().collect(Collectors.toMap(FileVo::getId, e -> e));
            }
            for (ProcessTaskStepTaskUserVo stepTaskUserVo : stepTaskUserList) {
                if (stepTaskUserVo.getEndTime() == null && stepTaskUserVo.getIsDelete() == 1) {
                    continue;
                }
                int isReplyable = 0;
                try {
                    Long processTaskStepId = stepTaskId2StepIdMap.get(stepTaskUserVo.getProcessTaskStepTaskId());
                    ProcessTaskStepVo processTaskStepVo = processTaskStepMap.get(processTaskStepId);
                    isReplyable = processTaskStepTaskService.checkIsReplyable(processTaskVo, processTaskStepVo, stepTaskUserVo.getUserUuid(), stepTaskUserVo.getId());
                } catch (ProcessTaskPermissionDeniedException processTaskPermissionDeniedException) {
                    isReplyable = 0;
                }
                stepTaskUserVo.setIsReplyable(isReplyable);
                String originalUserUuid = stepTaskUserAgentMap.get(stepTaskUserVo.getId());
                if (StringUtils.isNotBlank(originalUserUuid)) {
                    stepTaskUserVo.setOriginalUserUuid(originalUserUuid);
                    UserVo userVo = userMap.get(originalUserUuid);
                    if (userVo != null) {
                        UserVo originalUserVo = new UserVo();
                        BeanUtils.copyProperties(userVo, originalUserVo);
                        stepTaskUserVo.setOriginalUserVo(originalUserVo);
                    }
                }
                ProcessTaskStepTaskUserContentVo stepTaskUserContentVo = stepTaskUserContentMap.get(stepTaskUserVo.getId());
                if (stepTaskUserContentVo != null) {
                    stepTaskUserVo.setContent(stepTaskUserContentVo.getContent());
                    stepTaskUserVo.setProcessTaskStepTaskUserContentId(stepTaskUserContentVo.getId());
                }
                stepTaskUserMap.computeIfAbsent(stepTaskUserVo.getProcessTaskStepTaskId(), key -> new ArrayList<>()).add(stepTaskUserVo);

                List<Long> fileIdList = stepTaskUserFileIdListMap.get(stepTaskUserVo.getId());
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    stepTaskUserVo.setFileIdList(fileIdList);
                    List<FileVo> fileList = new ArrayList<>();
                    for (Long fileId : fileIdList) {
                        FileVo fileVo = fileMap.get(fileId);
                        if (fileVo != null) {
                            fileList.add(fileVo);
                        }
                    }
                    stepTaskUserVo.setFileList(fileList);
                }
            }
        }
        for (ProcessTaskStepTaskVo stepTaskVo : processTaskStepTaskList) {
            List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = stepTaskUserMap.get(stepTaskVo.getId());
            stepTaskVo.setStepTaskUserVoList(processTaskStepTaskUserList);
        }
        return processTaskStepTaskList;
    }
}
