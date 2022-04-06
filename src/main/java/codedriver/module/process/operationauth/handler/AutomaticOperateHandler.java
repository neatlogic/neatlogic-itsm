package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import codedriver.framework.process.exception.operationauth.ProcessTaskAutomaticHandlerNotEnableOperateException;
import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import com.alibaba.fastjson.JSONObject;
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
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RETREAT;
                //1.提示“自动处理节点不支持'撤回'操作”；
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskAutomaticHandlerNotEnableOperateException(operationType));
                return false;
            });
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_WORK;
                //1.提示“自动处理节点不支持'处理'操作”；
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskAutomaticHandlerNotEnableOperateException(operationType));
                return false;
            });
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMMENT;
                //1.提示“自动处理节点不支持'回复'操作”；
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
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
