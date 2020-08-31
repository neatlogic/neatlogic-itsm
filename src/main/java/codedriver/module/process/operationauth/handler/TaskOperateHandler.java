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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class TaskOperateHandler implements IOperationAuthHandler {

    private Map<ProcessTaskOperationType, Predicate<ProcessTaskVo>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ProcessTaskService processTaskService;
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.POCESSTASKVIEW, (processTaskVo) -> {
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else {
                List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                List<String> channelList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList(), processTaskVo.getChannelUuid());
                if (channelList.contains(processTaskVo.getChannelUuid())) {
                    return true;
                } else if (processTaskVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.WORKER.getValue())) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskVo) -> {
            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner()) || UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ABORT, (processTaskVo) -> {
            // 工单状态为进行中的才能终止
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
                    processTaskVo.getStepList().addAll(startProcessTaskStepList);
                    List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.PROCESS.getValue());
                    processTaskVo.getStepList().addAll(processTaskStepList);
                }
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        return processTaskService.checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.ABORT);
                    }
                }
            }          
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskVo) -> {
            // 工单状态为已终止的才能恢复
            if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
                    processTaskVo.getStepList().addAll(startProcessTaskStepList);
                    List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.PROCESS.getValue());
                    processTaskVo.getStepList().addAll(processTaskStepList);
                }
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == -1) {
                        return processTaskService.checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.ABORT);
                    }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskVo) -> {
            if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
                processTaskVo.getStepList().addAll(startProcessTaskStepList);
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.PROCESS.getValue());
                processTaskVo.getStepList().addAll(processTaskStepList);
            }
            for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                if (processTaskStep.getIsActive().intValue() == 1) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.UPDATE);
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskVo) -> {
            if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
                processTaskVo.getStepList().addAll(startProcessTaskStepList);
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.PROCESS.getValue());
                processTaskVo.getStepList().addAll(processTaskStepList);
            }
            for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                if (processTaskStep.getIsActive().intValue() == 1) {
                    return processTaskService.checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.URGE);
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo) -> {
            // 有可处理步骤work
            if (processTaskVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.WORKER.getValue())) {
                return true;
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskVo) -> {
            // 撤销权限retreat
            Set<ProcessTaskStepVo> retractableStepSet = processTaskService.getRetractableStepListByProcessTaskId(processTaskVo.getId());
            if (CollectionUtils.isNotEmpty(retractableStepSet)) {
                return true;
            }
            return false;
        });
        
    }
	@Override
	public Map<ProcessTaskOperationType, Boolean> getOperateMap(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
	    Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
	    for(Entry<ProcessTaskOperationType, Predicate<ProcessTaskVo>> entry :operationBiPredicateMap.entrySet()) {
	        resultMap.put(entry.getKey(), entry.getValue().test(processTaskVo));
	    }
		return resultMap;
	}
	
    @Override
    public Map<ProcessTaskOperationType, Boolean> getOperateMap(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo, List<ProcessTaskOperationType> operationTypeList) {
        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
        for(ProcessTaskOperationType operationType : operationTypeList) {
            Predicate<ProcessTaskVo> predicate = operationBiPredicateMap.get(operationType);
            if(predicate != null) {
                resultMap.put(operationType, predicate.test(processTaskVo));
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
