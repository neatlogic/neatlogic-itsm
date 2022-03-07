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
import codedriver.framework.process.exception.operationauth.*;
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
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>> operationBiPredicateMap = new HashMap<>();

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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_VIEW, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_VIEW;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(), ProcessTaskStatus.DRAFT);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }

            if (userUuid.equals(processTaskVo.getOwner())) {
                return true;
            } else if (userUuid.equals(processTaskVo.getReporter())) {
                return true;
            } else if (checkIsProcessTaskStepUser(processTaskStepVo, userUuid)) {
                return true;
            } else if (checkIsWorker(processTaskStepVo, userUuid)) {
                return true;
            }
            boolean flag = checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, operationType, userUuid);
            if (flag) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
            return false;
        });
        /**
         * 步骤转交权限
         * 判断userUuid用户是否有步骤转交权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户在步骤权限设置中获得“转交”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_TRANSFER,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_TRANSFER;
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
                ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                // 步骤状态为已激活的才能转交
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotActiveException());
                    return false;
                }
                exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                boolean flag = checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, operationType, userUuid);
                if (flag) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                return false;
            });
        /**
         * 步骤接受（抢单）权限
         * 判断userUuid用户是否有步骤接受权限逻辑：
         * 首先步骤状态是“已激活”且“待处理”，然后userUuid用户是步骤的待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACCEPT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_ACCEPT;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.RUNNING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (!checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotWorkerException());
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepMajorUserException());
                return false;
            }
            return true;
        });
        /**
         * 步骤开始权限
         * 判断userUuid用户是否有步骤开始权限逻辑：
         * 首先步骤状态是“已激活”且“待处理”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_START, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_START;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.RUNNING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
//            if (!checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
//                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
//                        .put(operationType, new ProcessTaskStepNotWorkerException());
//                return false;
//            }
            // 有主处理人时是start
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤流转权限
         * 判断userUuid用户是否有步骤流转权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有前进（实线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMPLETE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMPLETE;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            if (checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.FORWARD)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskStepNotNextStepException());
            return false;
        });
        /**
         * 步骤回退权限
         * 判断userUuid用户是否有步骤回退权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有回退（虚线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_BACK, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_BACK;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            if (checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.BACKWARD)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskStepBackNotEnabledException());
            return false;
        });
        /**
         * 步骤暂存权限
         * 判断userUuid用户是否有步骤暂存权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_SAVE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_SAVE;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }

            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤回复权限
         * 判断userUuid用户是否有步骤回复权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMMENT;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤暂停权限
         * 判断userUuid用户是否有步骤暂停权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_PAUSE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_PAUSE;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }

            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, operationType, userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
            return false;
        });
        /**
         * 步骤恢复权限
         * 判断userUuid用户是否有步骤恢复权限逻辑：
         * 首先步骤状态是“已激活”且“已挂起”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RECOVER, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RECOVER;
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }

            exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.RUNNING,
                    ProcessTaskStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_PAUSE, userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_PAUSE));
            return false;
        });
        /**
         * 步骤撤回权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“已完成”，然后userUuid用户在步骤权限设置中获得“撤回”的授权，当前步骤流转时激活步骤列表中有未完成的步骤
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RETREAT;
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
                ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                if (!ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepUndoneException());
                    return false;
                }
                // 撤销权限retreat
                if (!checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, operationType, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                    return false;
                }
                if (checkCurrentStepIsRetractableByProcessTaskStepId(processTaskVo, processTaskStepVo.getId(), userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepCannotRetreatException());
                return false;
            });
        /**
         * 步骤重审权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“处理中”，然后userUuid用户是步骤处理人，当前步骤是由回退线操作激活的
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_REAPPROVAL,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_REAPPROVAL;
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }

                    ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }

                    exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.PENDING);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotMajorUserException());
                        return false;
                    }
                    if (!Objects.equals(processTaskStepVo.getEnableReapproval(), 1)){
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepReapprovalNotEnabledException());
                        return false;
                    }
                    List<ProcessTaskStepRelVo> relList = processTaskVo.getStepRelList();
                    if (CollectionUtils.isNotEmpty(relList)) {
                        for (ProcessTaskStepRelVo processTaskStepRelVo : relList) {
                            if (Objects.equals(id, processTaskStepRelVo.getToProcessTaskStepId())) {
                                if (Objects.equals(processTaskStepRelVo.getType(), ProcessFlowDirection.BACKWARD.getValue()) && Objects.equals(processTaskStepRelVo.getIsHit(), 1)) {
                                    return true;
                                }
                            }
                        }
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNoNeedReapprovalStepException());
                    return false;
                });
        /**
         * 步骤处理权限
         * 判断userUuid用户是否有步骤处理权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户是步骤的处理人或待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_WORK;
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }

                ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotActiveException());
                    return false;
                }

                exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                if (checkIsWorker(processTaskStepVo, userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotWorkerException());
                return false;
            });
        /**
         * 步骤创建子任务权限
         * 判断userUuid用户是否有步骤创建子任务权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
//        operationBiPredicateMap.put(ProcessTaskOperationType.SUBTASK_CREATE,
//            (processTaskVo, processTaskStepVo, userUuid) -> {
//                if(processTaskVo.getIsShow() == 1) {
//                    if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
//                        return checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid);
//                    }
//                }
//                return false;
//            });

        /**
         * 步骤创建任务权限
         * 判断userUuid用户是否有步骤创建任务权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_CREATE,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.TASK_CREATE;
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }

                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotActiveException());
                        return false;
                    }

                    exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.PENDING);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotMajorUserException());
                    return false;
                });

        /**
         * 步骤删除任务权限
         * 判断userUuid用户是否有步骤删除任务权限逻辑：
         * userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_DELETE,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.TASK_DELETE;
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotActiveException());
                        return false;
                    }

                    exception = checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotMajorUserException());
                    return false;
                });
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.STEP.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
