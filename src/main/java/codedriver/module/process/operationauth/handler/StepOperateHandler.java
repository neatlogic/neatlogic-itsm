package codedriver.module.process.operationauth.handler;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class StepOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        /**
         * 步骤查看权限
         * 判断userUuid用户是否有步骤查看权限逻辑：
         * 首先工单状态不是“未提交”，
         * 符合一下几种情况之一就有步骤查看权限：
         * 1.userUuid用户是上报人
         * 2.userUuid用户是代报人
         * 3.userUuid用户是步骤的处理人或协助处理人
         * 4.userUuid用户在步骤权限设置中获得“查看节点信息”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_VIEW, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1 || AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {               
                if(!ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        return true;
                    } else if (userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    } else if (checkIsProcessTaskStepUser(processTaskStepVo, userUuid)) {
                        return true;
                    } else if (checkIsWorker(processTaskStepVo, userUuid)) {
                        return true;
                    } else {
                        return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_VIEW,
                            userUuid);
                    }               
                }
            }
            return false;
        });
        /**
         * 步骤转交权限
         * 判断userUuid用户是否有步骤转交权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户在步骤权限设置中获得“转交”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_TRANSFER,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if(processTaskVo.getIsShow() == 1) {
                    // 步骤状态为已激活的才能转交
                    if (processTaskStepVo.getIsActive() == 1) {
                        return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                            ProcessTaskOperationType.STEP_TRANSFER, userUuid);
                    }
                }
                return false;
            });
        /**
         * 步骤接受（抢单）权限
         * 判断userUuid用户是否有步骤接受权限逻辑：
         * 首先步骤状态是“已激活”且“待处理”，然后userUuid用户是步骤的待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACCEPT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {              
                if (processTaskStepVo.getIsActive() == 1) {
                    if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                        if (checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            // 没有主处理人时是accept
                            return !checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                        }
                    }
                }
            }
            return false;
        });
        /**
         * 步骤开始权限
         * 判断userUuid用户是否有步骤开始权限逻辑：
         * 首先步骤状态是“已激活”且“待处理”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_START, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {
                if (processTaskStepVo.getIsActive() == 1) {
                    if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {// 已激活未处理
                        if (checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            // 有主处理人时是start
                            return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                        }
                    }
                }
            }
            return false;
        });
        /**
         * 步骤流转权限
         * 判断userUuid用户是否有步骤流转权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有前进（实线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMPLETE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {              
                if (processTaskStepVo.getIsActive() == 1) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) 
                        || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                        if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            return checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo,
                                processTaskStepVo.getId(), ProcessFlowDirection.FORWARD);
                        }
                    }
                    
                }
            }
            return false;
        });
        /**
         * 步骤回退权限
         * 判断userUuid用户是否有步骤回退权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有回退（虚线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_BACK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {
                /** 考虑到取消工单的时候步骤状态不变，isActive=-1，所以这里要判断isActive是否等于1 **/
                if(processTaskStepVo.getIsActive() == 1){
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                        if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                            return checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo,
                                    processTaskStepVo.getId(), ProcessFlowDirection.BACKWARD);
                        }
                    }
                }
            }
            return false;
        });
        /**
         * 步骤暂存权限
         * 判断userUuid用户是否有步骤暂存权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_SAVE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {                
                if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                }
            }
            return false;
        });
        /**
         * 步骤回复权限
         * 判断userUuid用户是否有步骤回复权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {
                if (processTaskStepVo.getIsActive() == 1) {
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                        return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                    }
                }
            }
            return false;
        });
        /**
         * 步骤暂停权限
         * 判断userUuid用户是否有步骤暂停权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_PAUSE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {                
                if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                        ProcessTaskOperationType.STEP_PAUSE, userUuid);
                }
            }
            return false;
        });
        /**
         * 步骤恢复权限
         * 判断userUuid用户是否有步骤恢复权限逻辑：
         * 首先步骤状态是“已激活”且“已挂起”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RECOVER, (processTaskVo, processTaskStepVo, userUuid) -> {
            if(processTaskVo.getIsShow() == 1) {               
                if (processTaskStepVo.getIsActive() == 1) {
                    if (ProcessTaskStatus.HANG.getValue().equals(processTaskStepVo.getStatus())) {
                        return checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                            ProcessTaskOperationType.STEP_PAUSE, userUuid);
                    }
                }
            }
            return false;
        });
        /**
         * 步骤撤回权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“已完成”，然后userUuid用户在步骤权限设置中获得“撤回”的授权，当前步骤流转时激活步骤列表中有未完成的步骤
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if(processTaskVo.getIsShow() == 1) {
                    // 撤销权限retreat
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                        if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus())) {
                            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo,
                                ProcessTaskOperationType.STEP_RETREAT, userUuid)) {
                                return checkCurrentStepIsRetractableByProcessTaskStepId(processTaskVo,
                                    processTaskStepVo.getId(), userUuid);
                            }
                        }
                    }
                }
                return false;
            });
        /**
         * 步骤重审权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“处理中”，然后userUuid用户是步骤处理人，当前步骤是由回退线操作激活的
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_REAPPROVAL,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if(processTaskVo.getIsShow() == 1) {
                        if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                                if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                                    if (Objects.equals(processTaskStepVo.getEnableReapproval(), 1)){
                                        List<ProcessTaskStepRelVo> relList = processTaskVo.getStepRelList();
                                        if (CollectionUtils.isNotEmpty(relList)) {
                                            for (ProcessTaskStepRelVo processTaskStepRelVo : relList) {
                                                if (Objects.equals(processTaskStepVo.getId(), processTaskStepRelVo.getToProcessTaskStepId())) {
                                                    if (Objects.equals(processTaskStepRelVo.getType(), ProcessFlowDirection.BACKWARD.getValue()) && Objects.equals(processTaskStepRelVo.getIsHit(), 1)) {
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                });
        /**
         * 步骤处理权限
         * 判断userUuid用户是否有步骤处理权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户是步骤的处理人或待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if(processTaskVo.getIsShow() == 1) {
                    // 有可处理步骤work
                    if(processTaskStepVo.getIsActive() == 1) {
                        return checkIsWorker(processTaskStepVo, userUuid);
                    }
                }
                return false;
            });
        /**
         * 步骤创建子任务权限
         * 判断userUuid用户是否有步骤创建子任务权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.SUBTASK_CREATE,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if(processTaskVo.getIsShow() == 1) {
                    if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                        return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                    }
                }
                return false;
            });

        /**
         * 步骤创建任务权限
         * 判断userUuid用户是否有步骤创建任务权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_CREATE,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if(processTaskVo.getIsShow() == 1) {
                        if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                            return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                        }
                    }
                    return false;
                });

        /**
         * 步骤删除任务权限
         * 判断userUuid用户是否有步骤删除任务权限逻辑：
         * userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_DELETE,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if(processTaskVo.getIsShow() == 1) {
                        if (processTaskStepVo.getIsActive() == 1) {
                            return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
                        }
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
