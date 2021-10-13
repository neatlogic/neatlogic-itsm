package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.service.AuthenticationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskCurrentUserTaskListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Autowired
    private WorktimeMapper worktimeMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

    @Override
    public String getToken() {
        return "processtask/currentuser/task/list";
    }

    @Override
    public String getName() {
        return "当前用户任务列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字搜索"),
        @Param(name = "currentProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "当前工单id"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")})
    @Output({@Param(name = "taskList", type = ApiParamType.JSONARRAY, desc = "任务列表"),
        @Param(explode = BasePageVo.class)})
    @Description(desc = "当前用户任务列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long currentProcessTaskId = jsonObj.getLong("currentProcessTaskId");
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(currentProcessTaskId.toString());
        }
        String currentUserUuid = UserContext.get().getUserUuid(true);
        JSONObject resultObj = new JSONObject();
        ProcessTaskStepWorkerVo searchVo = JSON.toJavaObject(jsonObj, ProcessTaskStepWorkerVo.class);
        List<String> userUuidList = new ArrayList<>();
        userUuidList.add(currentUserUuid);
        //TODO linbq 后面再做
//        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(currentUserUuid, "processtask");
//        if (StringUtils.isNotBlank(userUuid)) {
//            userUuidList.add(userUuid);
//        }
        searchVo.setProcessTaskId(currentProcessTaskId);
        searchVo.setUserUuidList(userUuidList);
        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuidList);
        searchVo.setTeamUuidList(authenticationInfoVo.getTeamUuidList());
        searchVo.setRoleUuidList(authenticationInfoVo.getRoleUuidList());
        int rowNum = processTaskMapper.getProcessTaskStepWorkerCountByProcessTaskIdUserUuidTeamUuidListRoleUuidList(searchVo);
        if (rowNum > 0) {
            int pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
            if (!searchVo.getNeedPage() || searchVo.getCurrentPage() <= pageCount) {
                if (searchVo.getNeedPage()) {
                    resultObj.put("currentPage", searchVo.getCurrentPage());
                    resultObj.put("pageSize", searchVo.getPageSize());
                    resultObj.put("rowNum", rowNum);
                    resultObj.put("pageCount", pageCount);
                }
                List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper
                    .getProcessTaskStepWorkerListByProcessTaskIdUserUuidTeamUuidListRoleUuidList(searchVo);
                Set<Long> processTaskIdSet = new HashSet<>();
                List<Long> processTaskStepIdList = new ArrayList<>();
                for (ProcessTaskStepWorkerVo processTaskStepWorker : processTaskStepWorkerList) {
                    Long processTaskId = processTaskStepWorker.getProcessTaskId();
                    processTaskIdSet.add(processTaskId);
                    processTaskStepIdList.add(processTaskStepWorker.getProcessTaskStepId());
                }

                List<ProcessTaskVo> processTaskList = processTaskMapper
                    .getProcessTaskListByIdListAndStartTime(new ArrayList<>(processTaskIdSet), null, null);
                Map<Long, ProcessTaskVo> processTaskMap =
                    processTaskList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                List<ProcessTaskStepVo> processTaskStepList =
                    processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                processTaskStepList.sort((e1, e2) -> -e1.getId().compareTo(e2.getId()));
                Map<Long, ProcessTaskSlaTimeVo> stepSlaTimeMap = new HashMap<>();
                List<ProcessTaskSlaTimeVo> processTaskSlaTimeList =
                    processTaskMapper.getProcessTaskSlaTimeByProcessTaskStepIdList(processTaskStepIdList);
                for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
                    if (!stepSlaTimeMap.containsKey(processTaskSlaTimeVo.getProcessTaskStepId())) {
                        stepSlaTimeMap.put(processTaskSlaTimeVo.getProcessTaskStepId(), processTaskSlaTimeVo);
                    }
                }
                JSONArray taskList = new JSONArray();
                for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                    JSONObject task = new JSONObject();
                    ProcessTaskVo processTask = processTaskMap.get(processTaskStep.getProcessTaskId());
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTask.getChannelUuid());
                    if (channelVo != null) {
                        ChannelTypeVo channelTypeVo =
                            channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
                        if (channelTypeVo != null) {
                            task.put("prefix", channelTypeVo.getPrefix());
                        }
                    }
                    task.put("processTaskId", processTaskStep.getProcessTaskId());
                    task.put("title", processTask.getTitle());
                    task.put("serialNumber", processTask.getSerialNumber());
                    task.put("processTaskStepId", processTaskStep.getId());
                    task.put("stepName", processTaskStep.getName());
                    task.put("statusVo", processTaskStep.getStatusVo());

                    ProcessTaskSlaTimeVo processTaskSlaTimeVo = stepSlaTimeMap.get(processTaskStep.getId());
                    if (processTaskSlaTimeVo != null) {
                        if (processTaskSlaTimeVo.getExpireTime() != null) {
                            long timeLeft = 0L;
                            long nowTime = System.currentTimeMillis();
                            long expireTime = processTaskSlaTimeVo.getExpireTime().getTime();
                            if (nowTime < expireTime) {
                                timeLeft = worktimeMapper.calculateCostTime(processTask.getWorktimeUuid(), nowTime,
                                    expireTime);
                            } else if (nowTime > expireTime) {
                                timeLeft = -worktimeMapper.calculateCostTime(processTask.getWorktimeUuid(), expireTime,
                                    nowTime);
                            }
                            processTaskSlaTimeVo.setTimeLeft(timeLeft);
                        }
                        if (processTaskSlaTimeVo.getRealExpireTime() != null) {
                            long realTimeLeft =
                                processTaskSlaTimeVo.getExpireTime().getTime() - System.currentTimeMillis();
                            processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
                        }
                        task.put("slaTimeVo", processTaskSlaTimeVo);
                    }
                    taskList.add(task);
                }

                resultObj.put("taskList", taskList);
            }
        }
        return resultObj;
    }

}
