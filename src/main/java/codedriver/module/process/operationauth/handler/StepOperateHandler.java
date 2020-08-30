package codedriver.module.process.operationauth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class StepOperateHandler implements IOperationAuthHandler {

    private Map<ProcessTaskOperationType, BiPredicate<Long, Long>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private ProcessTaskService processTaskService;
	
	@PostConstruct
    public void init() {
	    operationBiPredicateMap.put(ProcessTaskOperationType.VIEW, (processTaskId, processTaskStepId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else if (processTaskMapper.checkIsProcessTaskStepUser(processTaskId, processTaskStepId, UserContext.get().getUserUuid(true)) > 0) {
                return true;
            }
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            return processTaskService.getProcessTaskStepConfigActionList(processTaskVo, processTaskStepVo, ProcessTaskOperationType.VIEW);
        });
	    
	    operationBiPredicateMap.put(ProcessTaskOperationType.TRANSFER, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            // 步骤状态为已激活的才能转交
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                return processTaskService.getProcessTaskStepConfigActionList(processTaskVo, processTaskStepVo, ProcessTaskOperationType.TRANSFER);
            }
            return false;
	    });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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

        operationBiPredicateMap.put(ProcessTaskOperationType.COMPLETE, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.BACK, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.SAVE, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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

        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = processTaskService.getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskId, processTaskStepId) -> {
            List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
            // 有可处理步骤work
            if (processTaskMapper.checkIsWorker(processTaskId, processTaskStepId, UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                return true;
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskId, processTaskStepId) -> {
            // 撤销权限retreat
            Set<ProcessTaskStepVo> retractableStepSet = processTaskService.getRetractableStepListByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(retractableStepSet)) {
                for(ProcessTaskStepVo processTaskStepVo : retractableStepSet) {
                    if(Objects.equals(processTaskStepId, processTaskStepVo.getId())) {
                        return true;
                    }
                }
            }
            return false;
        });
	}
	
	@Override
	public Map<ProcessTaskOperationType, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId) {
        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
        for(Entry<ProcessTaskOperationType, BiPredicate<Long, Long>> entry :operationBiPredicateMap.entrySet()) {
            resultMap.put(entry.getKey(), entry.getValue().test(processTaskId, processTaskStepId));
        }
        return resultMap;
	}
    
	@Override
    public Map<ProcessTaskOperationType, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId, List<ProcessTaskOperationType> operationTypeList) {
        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
        for(ProcessTaskOperationType operationType : operationTypeList) {
            BiPredicate<Long, Long> predicate = operationBiPredicateMap.get(operationType);
            if(predicate != null) {
                resultMap.put(operationType, predicate.test(processTaskId, processTaskStepId));
            }else {
                resultMap.put(operationType, false);
            }
        }    
        return resultMap;
    }

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.STEP;
	}
    
    @Override
    public List<ProcessTaskOperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }

}
