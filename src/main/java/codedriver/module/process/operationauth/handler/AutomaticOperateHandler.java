package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

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
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskVo, processTaskStepVo,
        // userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.START,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACTIVE,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREATCURRENTSTEP,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        operationBiPredicateMap.put(ProcessTaskOperationType.WORKCURRENTSTEP,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        // operationBiPredicateMap.put(ProcessTaskOperationType.ABORTPROCESSTASK, (processTaskVo, processTaskStepVo,
        // userUuid) -> false);
        // operationBiPredicateMap.put(ProcessTaskOperationType.RECOVERPROCESSTASK, (processTaskVo, processTaskStepVo,
        // userUuid) -> false);
        // operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskVo, processTaskStepVo, userUuid) ->
        // false);
        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT,
            (processTaskVo, processTaskStepVo, userUuid) -> false);
        // operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskVo, processTaskStepVo, userUuid) ->
        // false);
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.AUTOMATIC.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
