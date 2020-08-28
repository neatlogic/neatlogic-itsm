package codedriver.module.process.operationauth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class TaskOperateHandler extends OperationAuthHandlerBase {

    private static Map<ProcessTaskOperationType, Predicate<Long>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.POCESSTASKVIEW, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else {
                List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                List<String> channelList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList(), processTaskVo.getChannelUuid());
                if (channelList.contains(processTaskVo.getChannelUuid())) {
                    return true;
                } else if (processTaskMapper.checkIsWorker(processTaskId, null, UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                    return true;
                } else if (processTaskMapper.checkIsProcessTaskStepUser(processTaskId, null, UserContext.get().getUserUuid(true)) > 0) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner()) || UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ABORT, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            // 工单状态为进行中的才能终止
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                // 终止/恢复流程abort、修改上报内容update取工单当前所有正在处理的节点权限配置的并集
                List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
                processTaskStepList.addAll(startProcessTaskStepList);
                for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        return getProcessTaskStepConfigActionList(processTaskVo, processTaskStep, ProcessTaskOperationType.ABORT);
                    }
                }
            }          
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            // 工单状态为已终止的才能恢复
            if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                // 终止/恢复流程abort、修改上报内容update取工单当前所有正在处理的节点权限配置的并集
                List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
                processTaskStepList.addAll(startProcessTaskStepList);
                for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                    if (processTaskStep.getIsActive().intValue() == -1) {
                        return getProcessTaskStepConfigActionList(processTaskVo, processTaskStep, ProcessTaskOperationType.ABORT);
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            // 终止/恢复流程abort、修改上报内容update取工单当前所有正在处理的节点权限配置的并集
            List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
            processTaskStepList.addAll(startProcessTaskStepList);
            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                if (processTaskStep.getIsActive().intValue() == 1) {
                    return getProcessTaskStepConfigActionList(processTaskVo, processTaskStep, ProcessTaskOperationType.UPDATE);
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskId) -> {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            // 终止/恢复流程abort、修改上报内容update取工单当前所有正在处理的节点权限配置的并集
            List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
            processTaskStepList.addAll(startProcessTaskStepList);
            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                if (processTaskStep.getIsActive().intValue() == 1) {
                    return getProcessTaskStepConfigActionList(processTaskVo, processTaskStep, ProcessTaskOperationType.URGE);
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskId) -> {
            List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
            // 有可处理步骤work
            if (processTaskMapper.checkIsWorker(processTaskId, null, UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                return true;
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskId) -> {
            // 撤销权限retreat
            Set<ProcessTaskStepVo> retractableStepSet = getRetractableStepListByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(retractableStepSet)) {
                return true;
            }
            return false;
        });
        
    }
	@Override
	public Map<ProcessTaskOperationType, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId) {
	    Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
	    for(Entry<ProcessTaskOperationType, Predicate<Long>> entry :operationBiPredicateMap.entrySet()) {
	        resultMap.put(entry.getKey(), entry.getValue().test(processTaskId));
	    }
		return resultMap;
	}
	
    @Override
    public Map<ProcessTaskOperationType, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId, List<ProcessTaskOperationType> operationTypeList) {
        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
        for(ProcessTaskOperationType operationType : operationTypeList) {
            Predicate<Long> predicate = operationBiPredicateMap.get(operationType);
            if(predicate != null) {
                resultMap.put(operationType, predicate.test(processTaskId));
            }else {
                resultMap.put(operationType, false);
            }
        }    
        return resultMap;
    }

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.TASK;
	}
    
    @Override
    public List<ProcessTaskOperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }
}
