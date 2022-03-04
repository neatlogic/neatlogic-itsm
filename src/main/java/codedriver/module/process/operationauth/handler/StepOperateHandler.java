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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_VIEW, new ProcessTaskHiddenException());
                return false;
            }
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_VIEW, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_VIEW,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT
            )) {
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
            boolean flag = checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_VIEW, userUuid);
            if (flag) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_VIEW, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_VIEW));
            return false;
        });
        /**
         * 步骤转交权限
         * 判断userUuid用户是否有步骤转交权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户在步骤权限设置中获得“转交”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_TRANSFER,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_TRANSFER, new ProcessTaskHiddenException());
                    return false;
                }
                if (checkProcessTaskStatus(processTaskStepVo.getId(),
                        ProcessTaskOperationType.STEP_TRANSFER,
                        processTaskVo.getStatus(),
                        operationTypePermissionDeniedExceptionMap,
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED
                        )) {
                    return false;
                }
                // 步骤状态为已激活的才能转交
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_TRANSFER, new ProcessTaskStepNotActiveException());
                    return false;
                }
                if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                        ProcessTaskOperationType.STEP_TRANSFER,
                        processTaskStepVo.getStatus(),
                        operationTypePermissionDeniedExceptionMap,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG
                )) {
                    return false;
                }
                boolean flag = checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_TRANSFER, userUuid);
                if (flag) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_TRANSFER, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_TRANSFER));
                return false;
            });
        /**
         * 步骤接受（抢单）权限
         * 判断userUuid用户是否有步骤接受权限逻辑：
         * 首先步骤状态是“已激活”且“待处理”，然后userUuid用户是步骤的待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACCEPT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_ACCEPT, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_ACCEPT,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_ACCEPT, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_ACCEPT,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.RUNNING
            )) {
                return false;
            }
            if (!checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_ACCEPT, new ProcessTaskStepNotWorkerException());
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_ACCEPT, new ProcessTaskStepMajorUserException());
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_START, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_START,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_START, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_START,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.RUNNING
            )) {
                return false;
            }
//            if (!checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
//                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
//                        .put(ProcessTaskOperationType.STEP_START, new ProcessTaskStepNotWorkerException());
//                return false;
//            }
            // 有主处理人时是start
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_START, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤流转权限
         * 判断userUuid用户是否有步骤流转权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有前进（实线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMPLETE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_COMPLETE, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_COMPLETE,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_COMPLETE, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_COMPLETE,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_COMPLETE, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            if (checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.FORWARD)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_COMPLETE, new ProcessTaskStepNotNextStepException());
            return false;
        });
        /**
         * 步骤回退权限
         * 判断userUuid用户是否有步骤回退权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有回退（虚线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_BACK, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_BACK, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_BACK,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_BACK, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_BACK,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_BACK, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            if (checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.BACKWARD)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_BACK, new ProcessTaskStepBackNotEnabledException());
            return false;
        });
        /**
         * 步骤暂存权限
         * 判断userUuid用户是否有步骤暂存权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_SAVE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_SAVE, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_SAVE,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_SAVE, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_SAVE,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_SAVE, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤回复权限
         * 判断userUuid用户是否有步骤回复权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_COMMENT, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_COMMENT,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_COMMENT, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_COMMENT,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_COMMENT, new ProcessTaskStepNotMajorUserException());
            return false;
        });
        /**
         * 步骤暂停权限
         * 判断userUuid用户是否有步骤暂停权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_PAUSE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_PAUSE, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_PAUSE,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_PAUSE, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_PAUSE,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_PAUSE, userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_PAUSE, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_PAUSE));
            return false;
        });
        /**
         * 步骤恢复权限
         * 判断userUuid用户是否有步骤恢复权限逻辑：
         * 首先步骤状态是“已激活”且“已挂起”，然后userUuid用户在步骤权限设置中获得“暂停”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RECOVER, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_RECOVER, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_RECOVER,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_RECOVER, new ProcessTaskStepNotActiveException());
                return false;
            }
            if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                    ProcessTaskOperationType.STEP_RECOVER,
                    processTaskStepVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.RUNNING,
                    ProcessTaskStatus.PENDING
            )) {
                return false;
            }
            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_PAUSE, userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.STEP_PAUSE, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_PAUSE));
            return false;
        });
        /**
         * 步骤撤回权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“已完成”，然后userUuid用户在步骤权限设置中获得“撤回”的授权，当前步骤流转时激活步骤列表中有未完成的步骤
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_RETREAT, new ProcessTaskHiddenException());
                    return false;
                }
                if (checkProcessTaskStatus(processTaskStepVo.getId(),
                        ProcessTaskOperationType.STEP_RETREAT,
                        processTaskVo.getStatus(),
                        operationTypePermissionDeniedExceptionMap,
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED
                )) {
                    return false;
                }
                if (!ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_RETREAT, new ProcessTaskStepUndoneException());
                    return false;
                }
//                if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
//                        ProcessTaskOperationType.STEP_RECOVER,
//                        processTaskStepVo.getStatus(),
//                        operationTypePermissionDeniedExceptionMap,
//                        ProcessTaskStatus.HANG,
//                        ProcessTaskStatus.FAILED,
//                        ProcessTaskStatus.RUNNING,
//                        ProcessTaskStatus.PENDING
//                )) {
//                    return false;
//                }
                // 撤销权限retreat
                if (!checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_RETREAT, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_RETREAT, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.STEP_RETREAT));
                    return false;
                }
                if (checkCurrentStepIsRetractableByProcessTaskStepId(processTaskVo, processTaskStepVo.getId(), userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_RETREAT, new ProcessTaskStepCannotRetreatException());
                return false;
            });
        /**
         * 步骤重审权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“处理中”，然后userUuid用户是步骤处理人，当前步骤是由回退线操作激活的
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_REAPPROVAL,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.STEP_REAPPROVAL, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.STEP_REAPPROVAL,
                            processTaskVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED
                    )) {
                        return false;
                    }
                    if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.STEP_REAPPROVAL,
                            processTaskStepVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.PENDING
                    )) {
                        return false;
                    }
                    if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.STEP_REAPPROVAL, new ProcessTaskStepNotMajorUserException());
                        return false;
                    }
                    if (!Objects.equals(processTaskStepVo.getEnableReapproval(), 1)){
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.STEP_REAPPROVAL, new ProcessTaskStepReapprovalNotEnabledException());
                        return false;
                    }
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
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_REAPPROVAL, new ProcessTaskStepNoNeedReapprovalStepException());
                    return false;
                });
        /**
         * 步骤处理权限
         * 判断userUuid用户是否有步骤处理权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户是步骤的处理人或待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_WORK, new ProcessTaskHiddenException());
                    return false;
                }
                if (checkProcessTaskStatus(processTaskStepVo.getId(),
                        ProcessTaskOperationType.STEP_WORK,
                        processTaskVo.getStatus(),
                        operationTypePermissionDeniedExceptionMap,
                        ProcessTaskStatus.DRAFT,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.ABORTED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG,
                        ProcessTaskStatus.SCORED
                )) {
                    return false;
                }
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.STEP_WORK, new ProcessTaskStepNotActiveException());
                    return false;
                }
                if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                        ProcessTaskOperationType.STEP_WORK,
                        processTaskStepVo.getStatus(),
                        operationTypePermissionDeniedExceptionMap,
                        ProcessTaskStatus.SUCCEED,
                        ProcessTaskStatus.FAILED,
                        ProcessTaskStatus.HANG
                )) {
                    return false;
                }
                if (checkIsWorker(processTaskStepVo, userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.STEP_WORK, new ProcessTaskStepNotWorkerException());
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
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.TASK_CREATE, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.TASK_CREATE,
                            processTaskVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED
                    )) {
                        return false;
                    }
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.TASK_CREATE, new ProcessTaskStepNotActiveException());
                        return false;
                    }
                    if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.TASK_CREATE,
                            processTaskStepVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.PENDING
                    )) {
                        return false;
                    }
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.TASK_CREATE, new ProcessTaskStepNotMajorUserException());
                    return false;
                });

        /**
         * 步骤删除任务权限
         * 判断userUuid用户是否有步骤删除任务权限逻辑：
         * userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_DELETE,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.TASK_DELETE, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.TASK_DELETE,
                            processTaskVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.ABORTED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED
                    )) {
                        return false;
                    }
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.TASK_DELETE, new ProcessTaskStepNotActiveException());
                        return false;
                    }
                    if (checkProcessTaskStepStatus(processTaskStepVo.getId(),
                            ProcessTaskOperationType.TASK_DELETE,
                            processTaskStepVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG
                    )) {
                        return false;
                    }
                    if (checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskStepVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.TASK_DELETE, new ProcessTaskStepNotMajorUserException());
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
