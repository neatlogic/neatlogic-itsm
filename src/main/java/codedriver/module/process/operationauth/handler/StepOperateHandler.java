package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class StepOperateHandler implements IOperationAuthHandler {

    private final Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    
	@PostConstruct
    public void init() {
	    operationBiPredicateMap.put(ProcessTaskOperationType.VIEW, (processTaskVo, processTaskStepVo) -> {
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid())) > 0) {
                return true;
            } 
//            else if (processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
//                return true;
//            }else if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MINOR.getValue())) {
//                return true;
//            }
            return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.VIEW);
        });
	    
	    operationBiPredicateMap.put(ProcessTaskOperationType.TRANSFER, (processTaskVo, processTaskStepVo) -> {
            // 步骤状态为已激活的才能转交
            if (processTaskStepVo.getIsActive() == 1) {
                return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.TRANSFER);
            }
            return false;
	    });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid());
                    if(processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue(), UserContext.get().getUserUuid(), teamUuidList, UserContext.get().getRoleUuidList()) > 0) {
                        if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) == 0) {
                            // 没有主处理人时是accept
                            return true;
                        }
                    }
//                    if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.WORKER.getValue())) {
//                        if(!processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
//                         // 没有主处理人时是accept
//                            return true;
//                        }
//                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid());
                    if(processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue(), UserContext.get().getUserUuid(), teamUuidList, UserContext.get().getRoleUuidList()) > 0) {
                        if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                            // 有主处理人时是start
                            return true;
                        }
                    }
//                    if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.WORKER.getValue())) {
//                        if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
//                            // 有主处理人时是start
//                            return true;
//                        }
//                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMPLETE, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    //if (processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
                    if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                        List<ProcessTaskStepVo> forwardNextStepList = processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskStepVo.getId());
                        if(CollectionUtils.isNotEmpty(forwardNextStepList)) {
                            processTaskStepVo.setForwardNextStepList(forwardNextStepList);
                            return true;
                        }
                    }
                }
                
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.BACK, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    //if (processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
                    if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                        List<ProcessTaskStepVo> backwardNextStepList = processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskStepVo.getId());
                        if(CollectionUtils.isNotEmpty(backwardNextStepList)) {
                            processTaskStepVo.setBackwardNextStepList(backwardNextStepList);
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.SAVE, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    //if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                        return true;
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    //if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                        return true;
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.PAUSE, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.PAUSE);
                }              
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.HANG.getValue().equals(processTaskStepVo.getStatus())) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.PAUSE);
                }              
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREATCURRENTSTEP, (processTaskVo, processTaskStepVo) -> {
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    processTaskVo.getStepList().addAll(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId()));
                }
                Set<ProcessTaskStepVo> retractableStepSet = new HashSet<>();
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        retractableStepSet.addAll(processTaskService.getRetractableStepListByProcessTaskStepId(processTaskVo, processTaskStep.getId()));
                    }
                }
                if (CollectionUtils.isNotEmpty(retractableStepSet)) {
                    for(ProcessTaskStepVo processTaskStep : retractableStepSet) {
                        if(Objects.equals(processTaskStepVo.getId(), processTaskStep.getId())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo, processTaskStepVo) -> {
            // 有可处理步骤work
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid());
            if (processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), null, UserContext.get().getUserUuid(), teamUuidList, UserContext.get().getRoleUuidList()) > 0) {
                return true;
            }
            return false;
        });
	}

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.STEP;
	}
    @Override
    public Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
