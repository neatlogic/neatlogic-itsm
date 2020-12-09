package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
@Component
public class OmnipotentOperateHandler implements IOperationAuthHandler {

    private final Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.CREATESUBTASK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue())) > 0) {
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
