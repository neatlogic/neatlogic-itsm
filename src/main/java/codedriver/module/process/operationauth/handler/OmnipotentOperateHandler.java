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
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
@Component
public class OmnipotentOperateHandler extends OperationAuthHandlerBase {

    private static Map<ProcessTaskOperationType, BiPredicate<Long, Long>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.CREATESUBTASK, (processTaskId, processTaskStepId) -> {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo.getIsActive() == 1) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStepId);
                if (currentUserProcessUserTypeList.contains(ProcessUserType.WORKER.getValue())) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
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
    public IOperationAuthHandlerType getHandler() {
        return OperationAuthHandlerType.OMNIPOTENT;
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
    public List<ProcessTaskOperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }

}
