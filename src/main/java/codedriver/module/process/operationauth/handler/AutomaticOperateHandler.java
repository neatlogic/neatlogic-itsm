package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class AutomaticOperateHandler implements IOperationAuthHandler {

    private final Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACTIVE, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ABORT, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskVo, processTaskStepVo) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskVo, processTaskStepVo) -> false);
    }

//	@Override
//	public Map<ProcessTaskOperationType, Boolean> getOperateMap(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
//        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
//        for(Entry<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> entry :operationBiPredicateMap.entrySet()) {
//            resultMap.put(entry.getKey(), entry.getValue().test(processTaskVo, processTaskStepVo));
//        }
//        return resultMap;
//	}
//    
//    @Override
//    public Map<ProcessTaskOperationType, Boolean> getOperateMap(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo, List<ProcessTaskOperationType> operationTypeList) {
//        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
//        for(ProcessTaskOperationType operationType : operationTypeList) {
//            BiPredicate<ProcessTaskVo, ProcessTaskStepVo> predicate = operationBiPredicateMap.get(operationType);
//            if(predicate != null) {
//                resultMap.put(operationType, predicate.test(processTaskVo, processTaskStepVo));
//            }else {
//                resultMap.put(operationType, false);
//            }
//        }    
//        return resultMap;
//    }
//    
//    @Override
//    public List<ProcessTaskOperationType> getAllOperationTypeList() {      
//        return new ArrayList<>(operationBiPredicateMap.keySet());
//    }

    @Override
    public OperationAuthHandlerType getHandler() {
        return OperationAuthHandlerType.AUTOMATIC;
    }
    @Override
    public Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
