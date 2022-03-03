package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;

@Component
public class AutomaticOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
//        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_START,
//            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACTIVE,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> false);
//        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACCEPT,
//            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> false);
//        operationBiPredicateMap.put(ProcessTaskOperationType.SUBTASK_CREATE,
//            (processTaskVo, processTaskStepVo, userUuid) -> false);
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.AUTOMATIC.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
