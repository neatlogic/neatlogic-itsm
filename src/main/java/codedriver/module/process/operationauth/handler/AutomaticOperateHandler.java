package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import codedriver.framework.process.exception.operationauth.ProcessTaskAutomaticHandlerNotEnableOperateException;
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RETREAT;
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskAutomaticHandlerNotEnableOperateException(operationType));
                return false;
            });
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_WORK;
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskAutomaticHandlerNotEnableOperateException(operationType));
                return false;
            });
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMMENT;
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskAutomaticHandlerNotEnableOperateException(operationType));
                return false;
            });
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
