package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
@Component
public class OmnipotentOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.CREATESUBTASK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                }
            }
            return false;
        });
        
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.OMNIPOTENT.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
