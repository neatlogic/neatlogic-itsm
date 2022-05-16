package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.dao.mapper.ProcessMapper;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
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
            String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(processTaskStepList)) {
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    processTaskService.setProcessTaskStepUser(processTaskStepVo);
                    List<ProcessTaskStepUserVo> minorUserList = processTaskStepVo.getMinorUserList();
                    if (CollectionUtils.isNotEmpty(minorUserList)) {
                        setMinorUserTaskType(processTaskStepVo, minorUserList);
                    }
                    processTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(processTaskStepVo));
                    processTaskStepVo.setAssignableWorkerStepList(null);
                    processTaskStepVo.setBackwardNextStepList(null);
                    processTaskStepVo.setCommentList(null);
                    processTaskStepVo.setFormAttributeList(null);
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
            resultObj.put("config", JSONObject.parseObject(config));
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
            processTaskStepVo.setStatus(ProcessTaskStatus.DRAFT.getValue());
            processTaskStepVo.setStatusVo(new ProcessTaskStatusVo(ProcessTaskStatus.DRAFT.getValue()));
            processTaskStepVo.setAssignableWorkerStepList(null);
            processTaskStepVo.setBackwardNextStepList(null);
            processTaskStepVo.setCommentList(null);
            processTaskStepVo.setFormAttributeList(null);
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
            resultObj.put("config", processVo.getConfig());
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
        String stepConfigStr = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        JSONObject taskConfig = (JSONObject) JSONPath.read(stepConfigStr, "taskConfig");
        if (MapUtils.isNotEmpty(taskConfig)) {
            JSONArray idArray = taskConfig.getJSONArray("idList");
            if (CollectionUtils.isNotEmpty(idArray)) {
                List<TaskConfigVo> taskConfigList = taskMapper.getTaskConfigByIdList(idArray);
                if (CollectionUtils.isNotEmpty(taskConfigList)) {
                    Map<Long, TaskConfigVo> taskConfigMap = taskConfigList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                    List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskListByProcessTaskStepId(processTaskStepVo.getId());
                    if (CollectionUtils.isEmpty(processTaskStepTaskList)) {
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
                minorUserVo.setTaskType("变更步骤");
            }
        }
    }
}
