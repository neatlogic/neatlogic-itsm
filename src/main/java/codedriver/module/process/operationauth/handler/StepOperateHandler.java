package codedriver.module.process.operationauth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.constvalue.OperationType;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class StepOperateHandler extends OperationAuthHandlerBase {

    private static Map<OperationType, BiPredicate<Long, Long>> operationBiPredicateMap = new HashMap<>();
    
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private ProcessStepHandlerMapper processStepHandlerMapper;
	
	@PostConstruct
    public void init() {
	    operationBiPredicateMap.put(OperationType.VIEW, (processTaskId, processTaskStepId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else if (processTaskMapper.checkIsProcessTaskStepUser(processTaskId, processTaskStepId, UserContext.get().getUserUuid(true)) > 0) {
                return true;
            }
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            return getProcessTaskStepConfigActionList(processTaskVo, processTaskStepVo, OperationType.VIEW);
        });
	    
	    operationBiPredicateMap.put(OperationType.TRANSFER, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            // 步骤状态为已激活的才能转交
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                return getProcessTaskStepConfigActionList(processTaskVo, processTaskStepVo, OperationType.VIEW);
            }
            return false;
	    });
        
        operationBiPredicateMap.put(OperationType.ACCEPT, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                        if (!currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {// 没有主处理人时是accept
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(OperationType.START, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                        if (currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {// 有主处理人时是start
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(OperationType.COMPLETE, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                        // 完成complete 暂存save 评论comment 创建子任务createsubtask
                        if (currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(ProcessUserType.AGENT.getValue())) {
                            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,null);
                            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                                if (processTaskStep.getIsActive() != null) {
                                    if (ProcessFlowDirection.FORWARD.getValue().equals(processTaskStep.getFlowDirection())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(OperationType.BACK, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                        // 完成complete 暂存save 评论comment 创建子任务createsubtask
                        if (currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(ProcessUserType.AGENT.getValue())) {
                            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,null);
                            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                                if (processTaskStep.getIsActive() != null) {
                                    if (ProcessFlowDirection.BACKWARD.getValue().equals(processTaskStep.getFlowDirection()) && processTaskStep.getIsActive().intValue() != 0) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(OperationType.SAVE, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                        // 完成complete 暂存save 评论comment 创建子任务createsubtask
                        if (currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(ProcessUserType.AGENT.getValue())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(OperationType.COMMENT, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                        // 完成complete 暂存save 评论comment 创建子任务createsubtask
                        if (currentUserProcessUserTypeList.contains(ProcessUserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(ProcessUserType.AGENT.getValue())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
	}
	
	@Override
	public Map<String, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId) {
        Map<String, Boolean> resultMap = new HashMap<>();
        for(Entry<OperationType, BiPredicate<Long, Long>> entry :operationBiPredicateMap.entrySet()) {
            resultMap.put(entry.getKey().getValue(), entry.getValue().test(processTaskId, processTaskStepId));
        }
        return resultMap;
	}
    
    @Override
    public boolean getOperateMap(Long processTaskId, Long processTaskStepId, OperationType operationType) {
        BiPredicate<Long, Long> predicate = operationBiPredicateMap.get(operationType);
        if(predicate != null) {
            return predicate.test(processTaskId, processTaskStepId);
        }
        return false;
    }

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.STEP;
	}
    
    @Override
    public List<OperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }
	
	/**
     * 
     * @Time:2020年4月3日
     * @Description: 获取当前用户在当前步骤中工单干系人列表
     * @param processTaskVo     工单信息
     * @param processTaskStepId 步骤id
     * @return List<String>
     */
    private List<String> getCurrentUserProcessUserTypeList(ProcessTaskVo processTaskVo, Long processTaskStepId) {
        List<String> currentUserProcessUserTypeList = new ArrayList<>();
        currentUserProcessUserTypeList.add(UserType.ALL.getValue());
        if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
            currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
        }
        if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
            currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
        }
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
        List<String> majorUserUuidList = majorUserList.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList());
        if (majorUserUuidList.contains(UserContext.get().getUserUuid(true))) {
            currentUserProcessUserTypeList.add(ProcessUserType.MAJOR.getValue());
        }
        List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
        List<String> minorUserUuidList = minorUserList.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList());
        if (minorUserUuidList.contains(UserContext.get().getUserUuid(true))) {
            currentUserProcessUserTypeList.add(ProcessUserType.MINOR.getValue());
        }
        List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.AGENT.getValue());
        List<String> agentUserUuidList = agentUserList.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList());
        if (agentUserUuidList.contains(UserContext.get().getUserUuid(true))) {
            currentUserProcessUserTypeList.add(ProcessUserType.AGENT.getValue());
        }
        List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(workerList)) {
            List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
            for (ProcessTaskStepWorkerVo worker : workerList) {
                if (GroupSearch.USER.getValue().equals(worker.getType()) && UserContext.get().getUserUuid(true).equals(worker.getUuid())) {
                    currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                    break;
                } else if (GroupSearch.TEAM.getValue().equals(worker.getType()) && currentUserTeamList.contains(worker.getUuid())) {
                    currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                    break;
                } else if (GroupSearch.ROLE.getValue().equals(worker.getType()) && UserContext.get().getRoleUuidList().contains(worker.getUuid())) {
                    currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                    break;
                }
            }
        }

        return currentUserProcessUserTypeList;
    }
    /**
     * 
     * @Time:2020年4月2日
     * @Description: 获取流程节点配置中的当前用户的拥有的权限
     * @param processTaskVo
     * @param processTaskStepVo
     * @param actionList                     要获取的权限集合
     * @param currentUserProcessUserTypeList 当前用户工单干系人列表
     * @return List<String>
     */
    private boolean getProcessTaskStepConfigActionList(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo, OperationType operationType) {
        String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        JSONObject stepConfigObj = JSON.parseObject(stepConfig);
        JSONArray authorityList = stepConfigObj.getJSONArray("authorityList");
        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isEmpty(authorityList)) {
            ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
            if(processStepHandlerVo != null) {
                JSONObject handlerConfigObj = processStepHandlerVo.getConfig();
                if(MapUtils.isNotEmpty(handlerConfigObj)) {
                    authorityList = handlerConfigObj.getJSONArray("authorityList");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (int i = 0; i < authorityList.size(); i++) {
                JSONObject authorityObj = authorityList.getJSONObject(i);
                String action = authorityObj.getString("action");
                if(operationType.getValue().equals(action)) {
                    JSONArray acceptList = authorityObj.getJSONArray("acceptList");
                    if (CollectionUtils.isNotEmpty(acceptList)) {
                        List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepVo.getId());
                        List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                        for (int j = 0; j < acceptList.size(); j++) {
                            String accept = acceptList.getString(j);
                            String[] split = accept.split("#");
                            if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                if (currentUserProcessUserTypeList.contains(split[1])) {
                                    return true;
                                }
                            } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                                if (currentUserProcessUserTypeList.contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                if (currentUserTeamList.contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                if (UserContext.get().getRoleUuidList().contains(split[1])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
