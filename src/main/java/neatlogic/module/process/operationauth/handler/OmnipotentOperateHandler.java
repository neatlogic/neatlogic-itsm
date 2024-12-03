package neatlogic.module.process.operationauth.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.operationauth.core.OperationAuthHandlerBase;
import neatlogic.framework.process.operationauth.core.TernaryPredicate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class OmnipotentOperateHandler extends OperationAuthHandlerBase {

    private final Map<IOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<IOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {

    }

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.OMNIPOTENT.getHandler();
    }

    @Override
    public Map<IOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<IOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
