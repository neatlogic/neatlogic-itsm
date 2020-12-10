package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;

@Component
public class StepOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        operationBiPredicateMap.put(ProcessTaskOperationType.VIEW, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (userUuid.equals(processTaskVo.getOwner())) {
                return true;
            } else if (userUuid.equals(processTaskVo.getReporter())) {
                return true;
            } else if (checkIsProcessTaskStepUser(processTaskStepVo, userUuid)) {
                return true;
            }
            return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.VIEW,
                userUuid);
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TRANSFERCURRENTSTEP,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                // 步骤状态为已激活的才能转交
                if (processTaskStepVo.getIsActive() == 1) {
                    return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                        ProcessTaskOperationType.TRANSFERCURRENTSTEP, userUuid);
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.ACCEPT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    if (checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            // 没有主处理人时是accept
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.START, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                    if (checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            // 有主处理人时是start
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMPLETE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())
                    || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo,
                            processTaskStepVo.getId(), ProcessFlowDirection.FORWARD);
                        // List<ProcessTaskStepVo> forwardNextStepList =
                        // processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskStepVo.getId());
                        // if(CollectionUtils.isNotEmpty(forwardNextStepList)) {
                        // processTaskStepVo.setForwardNextStepList(forwardNextStepList);
                        // return true;
                        // }
                    }
                }

            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.BACK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())
                    || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo,
                            processTaskStepVo.getId(), ProcessFlowDirection.BACKWARD);
                        // List<ProcessTaskStepVo> backwardNextStepList =
                        // processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskStepVo.getId());
                        // if(CollectionUtils.isNotEmpty(backwardNextStepList)) {
                        // processTaskStepVo.setBackwardNextStepList(backwardNextStepList);
                        // return true;
                        // }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.SAVE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())
                    || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.COMMENT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())
                    || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.PAUSE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                        ProcessTaskOperationType.PAUSE, userUuid);
                }
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVER, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.HANG.getValue().equals(processTaskStepVo.getStatus())) {
                    return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                        ProcessTaskOperationType.PAUSE, userUuid);
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.RETREATCURRENTSTEP,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                // 撤销权限retreat
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus())) {
                        if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                            ProcessTaskOperationType.RETREATCURRENTSTEP, userUuid)) {
                            return checkCurrentStepIsRetractableByProcessTaskStepId(processTaskVo,
                                processTaskStepVo.getId(), userUuid);
                        }
                    }
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 有可处理步骤work
            if (checkIsWorker(processTaskStepVo, userUuid)) {
                return true;
            }
            return false;
        });
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.STEP.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
