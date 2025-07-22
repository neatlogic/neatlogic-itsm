package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepUserStatus;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTaskMapper;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFlowChartApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private ProcessTaskStepTaskMapper processTaskStepTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "processtask/flowchart";
    }

    @Override
    public String getName() {
        return "工单流程图";
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
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "工单id")
    })
    @Output({
            @Param(name = "config", explode = ProcessTaskStepVo[].class, desc = "流程图信息"),
            @Param(name = "processTaskStepList", explode = ProcessTaskStepVo[].class, desc = "步骤状态列表"),
            @Param(name = "processTaskStepRelList", explode = ProcessTaskStepVo[].class, desc = "连线状态列表")
    })
    @Description(desc = "工单流程图")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        String channelUuid = jsonObj.getString("channelUuid");
        if (processTaskId != null) {
            ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
            String configStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
            JSONObject config = JSONObject.parseObject(configStr);
            if (MapUtils.isNotEmpty(config)) {
                JSONObject processObj = config.getJSONObject("process");
                if (MapUtils.isNotEmpty(processObj)) {
                    JSONObject processConfig = processObj.getJSONObject("processConfig");
                    if (MapUtils.isNotEmpty(processConfig)) {
                        processConfig.put("uuid", processTaskVo.getProcessUuid());
                    }
                }
            }
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(processTaskStepList)) {
                Set<String> userUuidSet = new HashSet<>();
                Set<String> teamUuidSet = new HashSet<>();
                Set<String> roleUuidSet = new HashSet<>();
                ProcessTaskStepUserVo searchStepUserVo = new ProcessTaskStepUserVo();
                searchStepUserVo.setProcessTaskId(processTaskId);
                List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserList(searchStepUserVo);
                for (ProcessTaskStepUserVo stepUserVo : processTaskStepUserList) {
                    userUuidSet.add(stepUserVo.getUserUuid());
                }
                List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskId, null);
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
                    List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
                    teamMap = teamList.stream().collect(Collectors.toMap(TeamVo::getUuid, e -> e));
                }
                List<String> roleUuidList = roleUuidSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(roleUuidList)) {
                    List<RoleVo> roleList = roleMapper.getRoleByUuidList(roleUuidList);
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
                    processTaskService.setProcessTaskStepUser(processTaskStepVo, processTaskStepUserList, processTaskStepWorkerList);
                    List<ProcessTaskStepUserVo> minorUserList = processTaskStepVo.getMinorUserList();
                    if (CollectionUtils.isNotEmpty(minorUserList)) {
                        setMinorUserTaskType(processTaskStepVo, minorUserList);
                    }
                    processTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(processTaskStepVo));
                    processTaskStepVo.setCustomStatusList(processTaskService.getCustomStatusList(processTaskStepVo));
                    processTaskStepVo.setCustomButtonList(processTaskService.getCustomButtonList(processTaskStepVo));
                    processTaskStepVo.setAssignableWorkerStepList(null);
                    processTaskStepVo.setBackwardNextStepList(null);
                    processTaskStepVo.setCommentList(null);
//                    processTaskStepVo.setFormAttributeList(null);
                    processTaskStepVo.setFormAttributeVoList(null);
                    processTaskStepVo.setForwardNextStepList(null);
                    processTaskStepVo.setIsCurrentUserDone(null);
                    processTaskStepVo.setProcessTaskStepRemindList(null);
                    processTaskStepVo.setSlaTimeList(null);
                    processTaskStepVo.setUserList(null);
                    processTaskStepVo.setWorkerPolicyList(null);
                }
            }
            List<ProcessTaskStepRelVo> processTaskStepRelVoList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
            JSONObject resultObj = new JSONObject();
            resultObj.put("config", config);
            resultObj.put("processTaskStepList", processTaskStepList);
            resultObj.put("processTaskStepRelList", processTaskStepRelVoList);
            return resultObj;
        } else if (channelUuid != null) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channelUuid);
            }
            String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
            ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
            if (processVo == null) {
                throw new ProcessNotFoundException(processUuid);
            }
            Date startTime = new Date();
            ProcessStepVo processStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processStepVo);
            processTaskStepVo.setIsAutoGenerateId(false);
            processTaskStepVo.setStartTime(startTime);
            processTaskStepVo.setIsActive(1);
            processTaskStepVo.setStatus(ProcessTaskStepStatus.DRAFT.getValue());
            processTaskStepVo.setStatusVo(new ProcessTaskStepStatusVo(ProcessTaskStepStatus.DRAFT.getValue()));
            processTaskStepVo.setAssignableWorkerStepList(null);
            processTaskStepVo.setBackwardNextStepList(null);
            processTaskStepVo.setCommentList(null);
//            processTaskStepVo.setFormAttributeList(null);
            processTaskStepVo.setFormAttributeVoList(null);
            processTaskStepVo.setForwardNextStepList(null);
            processTaskStepVo.setIsCurrentUserDone(null);
            processTaskStepVo.setProcessTaskStepRemindList(null);
            processTaskStepVo.setSlaTimeList(null);
            processTaskStepVo.setUserList(null);
            processTaskStepVo.setWorkerPolicyList(null);
            ProcessTaskStepUserVo majorUser = new ProcessTaskStepUserVo();
            majorUser.setUserUuid(UserContext.get().getUserUuid(true));
            majorUser.setUserName(UserContext.get().getUserName());
            majorUser.setUserType(ProcessUserType.MAJOR.getValue());
            majorUser.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
            majorUser.setStartTime(startTime);
            processTaskStepVo.setMajorUser(majorUser);
            List<ProcessTaskStepVo> processTaskStepList = new ArrayList<>();
            processTaskStepList.add(processTaskStepVo);
            JSONObject resultObj = new JSONObject();
            JSONObject config = processVo.getConfig();
            if (MapUtils.isNotEmpty(config)) {
                JSONObject processObj = config.getJSONObject("process");
                if (MapUtils.isNotEmpty(processObj)) {
                    JSONObject processConfig = processObj.getJSONObject("processConfig");
                    if (MapUtils.isNotEmpty(processConfig)) {
                        processConfig.put("uuid", processVo.getUuid());
                    }
                }
            }
            resultObj.put("config", config);
            resultObj.put("processTaskStepList", processTaskStepList);
            resultObj.put("processTaskStepRelList", new ArrayList<>());
            return resultObj;
        } else {
            throw new ParamNotExistsException("processTaskId", "channelUuid");
        }
    }

    /**
     * 设置协助处理人对应当的任务类型
     *
     * @param processTaskStepVo
     * @param minorUserList
     */
    private void setMinorUserTaskType(ProcessTaskStepVo processTaskStepVo, List<ProcessTaskStepUserVo> minorUserList) {
        Map<String, Set<String>> userUuidTaskConfigNameMap = new HashMap<>();
        JSONObject stepConfigObj = processTaskStepVo.getConfig();
        if (stepConfigObj == null) {
            String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
            if (StringUtils.isNotBlank(config)) {
                stepConfigObj = JSONObject.parseObject(config);
            } else {
                stepConfigObj = new JSONObject();
            }
            processTaskStepVo.setConfig(stepConfigObj);
        }
        JSONObject taskConfig = stepConfigObj.getJSONObject("taskConfig");
        if (MapUtils.isNotEmpty(taskConfig)) {
            JSONArray idArray = taskConfig.getJSONArray("idList");
            if (CollectionUtils.isNotEmpty(idArray)) {
                List<TaskConfigVo> taskConfigList = taskMapper.getTaskConfigByIdList(idArray);
                if (CollectionUtils.isNotEmpty(taskConfigList)) {
                    Map<Long, TaskConfigVo> taskConfigMap = taskConfigList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                    List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskListByProcessTaskStepId(processTaskStepVo.getId());
                    if (CollectionUtils.isNotEmpty(processTaskStepTaskList)) {
                        Map<Long, ProcessTaskStepTaskVo> stepTaskIdMap = processTaskStepTaskList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                        List<Long> stepTaskIdList = processTaskStepTaskList.stream().map(ProcessTaskStepTaskVo::getId).collect(Collectors.toList());
                        List<ProcessTaskStepTaskUserVo> stepTaskUserList = processTaskStepTaskMapper.getStepTaskUserByStepTaskIdList(stepTaskIdList);
                        for (ProcessTaskStepTaskUserVo stepTaskUserVo : stepTaskUserList) {
                            if (Objects.equals(stepTaskUserVo.getIsDelete(), 1)) {
                                continue;
                            }
                            ProcessTaskStepTaskVo processTaskStepTaskVo = stepTaskIdMap.get(stepTaskUserVo.getProcessTaskStepTaskId());
                            if (processTaskStepTaskVo == null) {
                                continue;
                            }
                            TaskConfigVo taskConfigVo = taskConfigMap.get(processTaskStepTaskVo.getTaskConfigId());
                            if (taskConfigVo == null) {
                                continue;
                            }
                            if (StringUtils.isBlank(taskConfigVo.getName())) {
                                continue;
                            }
                            Set<String> taskConfigNameSet = userUuidTaskConfigNameMap.computeIfAbsent(stepTaskUserVo.getUserUuid(), key -> new HashSet<>());
                            taskConfigNameSet.add(taskConfigVo.getName());
                        }
                    }
                }
            }
        }
        for (ProcessTaskStepUserVo minorUserVo : minorUserList) {
            Set<String> taskConfigNameSet = userUuidTaskConfigNameMap.get(minorUserVo.getUserUuid());
            if (CollectionUtils.isNotEmpty(taskConfigNameSet)) {
                minorUserVo.setTaskType(String.join("、", taskConfigNameSet));
            } else {
                IProcessStepHandler stepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
                if (stepHandler != null) {
                    minorUserVo.setTaskType(stepHandler.getMinorName());
                }
            }
        }
    }
}
