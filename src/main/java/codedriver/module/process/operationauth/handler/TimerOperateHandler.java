package codedriver.module.process.operationauth.handler;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class TimerOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACTIVE,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.TIMER.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
