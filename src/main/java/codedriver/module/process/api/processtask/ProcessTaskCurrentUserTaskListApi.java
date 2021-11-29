package codedriver.module.process.api.processtask;

import java.util.*;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAgentMapper;
import codedriver.framework.process.dto.agent.ProcessTaskAgentVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
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

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private WorktimeMapper worktimeMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;

    @Resource
    private ProcessTaskAgentService processTaskAgentService;

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

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字搜索"),
            @Param(name = "currentProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "当前工单id"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
    })
    @Output({
            @Param(name = "taskList", type = ApiParamType.JSONARRAY, desc = "任务列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "当前用户任务列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long currentProcessTaskId = jsonObj.getLong("currentProcessTaskId");
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(currentProcessTaskId.toString());
        }
        List<Long> currentProcessTaskProcessableStepIdList = new ArrayList<>();
        List<ProcessTaskStepVo> currentProcessTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(currentProcessTaskId);
        List<Long> currentProcessTaskStepIdList = currentProcessTaskStepList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = new ProcessAuthManager.Builder().addProcessTaskStepId(currentProcessTaskStepIdList).build().getOperateMap();
        for (Map.Entry<Long, Set<ProcessTaskOperationType>> entry : operationTypeSetMap.entrySet()) {
            for (ProcessTaskOperationType operationType : entry.getValue()) {
                if (ProcessTaskOperationType.STEP_COMPLETE.getValue().equals(operationType.getValue())) {
                    currentProcessTaskProcessableStepIdList.add(entry.getKey());
                    break;
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        Set<Long> allProcessTaskStepIdSet = new HashSet<>();
        String keyword = jsonObj.getString("keyword");
        String currentUserUuid = UserContext.get().getUserUuid(true);
        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(currentUserUuid);
        Set<Long> processTaskStepIdSet = processTaskMapper.getProcessTaskStepIdSetByChannelUuidListAndAuthenticationInfo(keyword, null, authenticationInfoVo);
        allProcessTaskStepIdSet.addAll(processTaskStepIdSet);
        List<ProcessTaskAgentVo> processTaskAgentList = processTaskAgentMapper.getProcessTaskAgentListByToUserUuid(currentUserUuid);
        for (ProcessTaskAgentVo processTaskAgentVo : processTaskAgentList) {
            List<String> channelUuidList = processTaskAgentService.getChannelUuidListByProcessTaskAgentId(processTaskAgentVo.getId());
            if (CollectionUtils.isNotEmpty(channelUuidList)) {
                authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(processTaskAgentVo.getFromUserUuid());
                processTaskStepIdSet = processTaskMapper.getProcessTaskStepIdSetByChannelUuidListAndAuthenticationInfo(keyword, channelUuidList, authenticationInfoVo);
                allProcessTaskStepIdSet.addAll(processTaskStepIdSet);
            }
        }
        allProcessTaskStepIdSet.removeAll(currentProcessTaskProcessableStepIdList);
        List<Long> allProcessTaskStepIdList = new ArrayList<>(allProcessTaskStepIdSet);
        allProcessTaskStepIdList.sort(Comparator.reverseOrder());
        currentProcessTaskProcessableStepIdList.sort(Comparator.reverseOrder());
        allProcessTaskStepIdList.addAll(0, currentProcessTaskProcessableStepIdList);
        int rowNum = allProcessTaskStepIdList.size();
        BasePageVo searchVo = JSONObject.toJavaObject(jsonObj, BasePageVo.class);
        searchVo.setRowNum(rowNum);
        if (searchVo.getCurrentPage() <= searchVo.getPageCount()) {
            int fromIndex = searchVo.getStartNum();
            int toIndex = fromIndex + searchVo.getPageSize();
            toIndex = toIndex >  rowNum ? rowNum : toIndex;
            List<Long> currentPageProcessTaskStepIdList =  allProcessTaskStepIdList.subList(fromIndex, toIndex);
            if (CollectionUtils.isNotEmpty(currentPageProcessTaskStepIdList)) {
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(currentPageProcessTaskStepIdList);
                Map<Long, ProcessTaskStepVo> processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
                Map<Long, ProcessTaskVo> processTaskMap = processTaskList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                Map<Long, ProcessTaskSlaTimeVo> stepSlaTimeMap = new HashMap<>();
                List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskMapper.getProcessTaskSlaTimeByProcessTaskStepIdList(currentPageProcessTaskStepIdList);
                for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
                    if (!stepSlaTimeMap.containsKey(processTaskSlaTimeVo.getProcessTaskStepId())) {
                        stepSlaTimeMap.put(processTaskSlaTimeVo.getProcessTaskStepId(), processTaskSlaTimeVo);
                    }
                }
                JSONArray taskList = new JSONArray();
                for (Long processTaskStepId : currentPageProcessTaskStepIdList) {
                    JSONObject task = new JSONObject();
                    ProcessTaskStepVo processTaskStep = processTaskStepMap.get(processTaskStepId);
                    ProcessTaskVo processTask = processTaskMap.get(processTaskStep.getProcessTaskId());
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTask.getChannelUuid());
                    if (channelVo != null) {
                        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
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
                        parse(processTaskSlaTimeVo, processTask.getWorktimeUuid());
                        task.put("slaTimeVo", processTaskSlaTimeVo);
                    }
                    taskList.add(task);
                }
                resultObj = TableResultUtil.getResult(taskList, searchVo);
            }
        }
        return resultObj;
    }

    private void parse(ProcessTaskSlaTimeVo processTaskSlaTimeVo, String worktimeUuid) {
        Long expireTimeLong = processTaskSlaTimeVo.getExpireTimeLong();
        if (expireTimeLong != null) {
            long timeLeft = 0L;
            long nowTime = System.currentTimeMillis();
            if (nowTime < expireTimeLong) {
                timeLeft = worktimeMapper.calculateCostTime(worktimeUuid, nowTime, expireTimeLong);
            } else if (nowTime > expireTimeLong) {
                timeLeft = -worktimeMapper.calculateCostTime(worktimeUuid, expireTimeLong, nowTime);
            }
            processTaskSlaTimeVo.setTimeLeft(timeLeft);
        }
        Long realExpireTimeLong = processTaskSlaTimeVo.getRealExpireTimeLong();
        if (realExpireTimeLong != null) {
            long realTimeLeft = realExpireTimeLong - System.currentTimeMillis();
            processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
        }
    }
}
