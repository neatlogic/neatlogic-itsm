package codedriver.module.process.operationauth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class AutomaticOperateHandler implements IOperationAuthHandler {

    private Map<ProcessTaskOperationType, BiPredicate<Long, Long>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACTIVE, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ABORT, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskId, processTaskStepId) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskId, processTaskStepId) -> false);
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
		return OperationAuthHandlerType.AUTOMATIC;
	}
    
    @Override
    public List<ProcessTaskOperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }

}
