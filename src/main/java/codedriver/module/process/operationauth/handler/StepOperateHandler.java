package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class StepOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private UserMapper userMapper;
    
	@PostConstruct
    public void init() {
	    operationBiPredicateMap.put(ProcessTaskOperationType.VIEW, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (userUuid.equals(processTaskVo.getOwner())) {
                return true;
            } else if (userUuid.equals(processTaskVo.getReporter())) {
                return true;
            } else if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid)) > 0) {
                return true;
            }
            return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.VIEW, userUuid);
        });
	    
	    operationBiPredicateMap.put(ProcessTaskOperationType.TRANSFERCURRENTSTEP, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 步骤状态为已激活的才能转交
            if (processTaskStepVo.getIsActive() == 1) {
                return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.TRANSFERCURRENTSTEP, userUuid);
            }
            return false;
	    });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                    List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
                    if(processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue(), userUuid, teamUuidList, roleUuidList) > 0) {
                        if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) == 0) {
                            // 没有主处理人时是accept
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                    List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
                    if(processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue(), userUuid, teamUuidList, roleUuidList) > 0) {
                        if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
                            // 有主处理人时是start
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMPLETE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.BACK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.SAVE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
                        return true;
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
                        return true;
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.PAUSE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.PAUSE, userUuid);
                }              
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.HANG.getValue().equals(processTaskStepVo.getStatus())) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStepVo, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.PAUSE, userUuid);
                }              
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREATCURRENTSTEP, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    processTaskVo.getStepList().addAll(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId()));
                }
                Set<ProcessTaskStepVo> retractableStepSet = new HashSet<>();
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        retractableStepSet.addAll(processTaskService.getRetractableStepListByProcessTaskStepId(processTaskVo, processTaskStep.getId(), userUuid));
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 有可处理步骤work
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
            List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
            if (processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), null, userUuid, teamUuidList, roleUuidList) > 0) {
                return true;
            }
            return false;
        });
	}

	@Override
	public String getHandler() {
		return OperationAuthHandlerType.STEP.getValue();
	}
    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
