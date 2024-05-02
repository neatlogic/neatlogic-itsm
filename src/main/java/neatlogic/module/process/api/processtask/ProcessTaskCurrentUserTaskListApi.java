package neatlogic.module.process.api.processtask;

import java.util.*;
import java.util.stream.Collectors;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.service.ProcessTaskAgentService;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAgentMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskCurrentUserTaskListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

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
        return "nmpap.processtaskcurrentusertasklistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "common.keyword"),
            @Param(name = "currentProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.currentprocesstaskid"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage")
    })
    @Output({
            @Param(name = "taskList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpap.processtaskcurrentusertasklistapi.getname")
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
        Set<ProcessTaskOperationType> processableOperationSet = new HashSet<>();
        processableOperationSet.add(ProcessTaskOperationType.STEP_COMPLETE);
        processableOperationSet.add(ProcessTaskOperationType.STEP_RECOVER);
        processableOperationSet.add(ProcessTaskOperationType.STEP_ACCEPT);
        processableOperationSet.add(ProcessTaskOperationType.STEP_START);
        for (Map.Entry<Long, Set<ProcessTaskOperationType>> entry : operationTypeSetMap.entrySet()) {
            if (entry.getValue().removeAll(processableOperationSet)) {
                currentProcessTaskProcessableStepIdList.add(entry.getKey());
            }
        }
        JSONObject resultObj = new JSONObject();
        Set<Long> allProcessTaskStepIdSet = new HashSet<>();
        String keyword = jsonObj.getString("keyword");
        String currentUserUuid = UserContext.get().getUserUuid(true);
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
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
                List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskSlaMapper.getProcessTaskSlaTimeByProcessTaskStepIdList(currentPageProcessTaskStepIdList);
                for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
                    if (processTaskSlaTimeVo.getStatus().equals(SlaStatus.DOING.toString().toLowerCase())) {
                        if (!stepSlaTimeMap.containsKey(processTaskSlaTimeVo.getProcessTaskStepId())) {
                            stepSlaTimeMap.put(processTaskSlaTimeVo.getProcessTaskStepId(), processTaskSlaTimeVo);
                        }
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
                        task.put("slaTimeVo", processTaskSlaTimeVo);
                    }
                    taskList.add(task);
                }
                resultObj = TableResultUtil.getResult(taskList, searchVo);
            }
        }
        return resultObj;
    }
}
