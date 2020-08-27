package codedriver.module.process.operationauth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.OperationType;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class AutomaticOperateHandler extends OperationAuthHandlerBase {

    private static Map<OperationType, BiPredicate<Long, Long>> operationBiPredicateMap = new HashMap<>();

//	@Autowired
//	private ProcessTaskMapper processTaskMapper;
    
    @PostConstruct
    public void init() {
        
    }

	@Override
	public Map<String, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId) {
        Map<String, Boolean> resultMap = new HashMap<>();
        for(Entry<OperationType, BiPredicate<Long, Long>> entry :operationBiPredicateMap.entrySet()) {
            resultMap.put(entry.getKey().getValue(), entry.getValue().test(processTaskId, processTaskStepId));
        }
        return resultMap;
	}
    
    @Override
    public boolean getOperateMap(Long processTaskId, Long processTaskStepId, OperationType operationType) {
        BiPredicate<Long, Long> predicate = operationBiPredicateMap.get(operationType);
        if(predicate != null) {
            return predicate.test(processTaskId, processTaskStepId);
        }
        return false;
    }

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.AUTOMATIC;
	}
    
    @Override
    public List<OperationType> getAllOperationTypeList() {      
        return new ArrayList<>(operationBiPredicateMap.keySet());
    }

}
