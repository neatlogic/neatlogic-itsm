package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
@Component
public class OmnipotentOperateHandler implements IOperationAuthHandler {

    private final Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.CREATESUBTASK, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.MAJOR.getValue())) {
                        return true;
                    }
                    if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(ProcessUserType.AGENT.getValue())) {
                        return true;
                    }
                }
            }
            return false;
        });
        
    }

    @Override
    public IOperationAuthHandlerType getHandler() {
        return OperationAuthHandlerType.OMNIPOTENT;
    }

//    @Override
//    public Map<ProcessTaskOperationType, Boolean> getOperateMap(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
//        Map<ProcessTaskOperationType, Boolean> resultMap = new HashMap<>();
//        for(Entry<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> entry :operationBiPredicateMap.entrySet()) {
//            resultMap.put(entry.getKey(), entry.getValue().test(processTaskVo, processTaskStepVo));
//        }
//        return resultMap;
//    }
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
    public Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}