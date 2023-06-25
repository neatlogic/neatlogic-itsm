package neatlogic.module.process.operationauth.handler;

import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.*;
import neatlogic.framework.process.operationauth.core.OperationAuthHandlerBase;
import neatlogic.framework.process.operationauth.core.OperationAuthHandlerType;
import neatlogic.framework.process.operationauth.core.TernaryPredicate;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class StepOperateHandler extends OperationAuthHandlerBase {

    private final Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>> operationBiPredicateMap = new HashMap<>();

    @Resource
    private UserMapper userMapper;
    @Resource
    private ProcessTaskService processTaskService;

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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_VIEW, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_VIEW;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                //判断当前用户是否有“工单管理权限”或者是系统用户，如果两者都没有，则提示“工单已隐藏”；
                if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName()) && !SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(), ProcessTaskStatus.DRAFT);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //4.依次判断当前用户是否是工单上报人、代报人、处理人、待处理人，如果都不是，则执行第5步；
            if (userUuid.equals(processTaskVo.getOwner())) {
                return true;
            } else if (userUuid.equals(processTaskVo.getReporter())) {
                return true;
            } else if (checkIsProcessTaskStepUser(processTaskStepVo, userUuid)) {
                return true;
            } else if (checkIsWorker(processTaskStepVo, userUuid)) {
                return true;
            }
            //5.判断当前用户是否有当前步骤“查看节点信息”操作权限，如果没有，则提示“您的'查看节点信息'操作未获得授权”；
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
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_TRANSFER;
                //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
                //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
                // 步骤状态为已激活的才能转交
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotActiveException());
                    return false;
                }
                //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
                //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
                exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                        ProcessTaskStepStatus.FAILED,
                        ProcessTaskStepStatus.HANG);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //12.判断当前用户是否有当前步骤“转交”操作权限，如果没有，则提示“您的'转交'操作未获得授权”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_ACCEPT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_ACCEPT;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //10.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (ProcessTaskStepStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStepStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStepStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {
                List<ProcessTaskStepUserVo> userList = processTaskStepVo.getUserList();
                if (CollectionUtils.isNotEmpty(userList)) {
                    for (ProcessTaskStepUserVo processTaskStepUserVo : userList) {
                        if (Objects.equals(processTaskStepUserVo.getUserType(), ProcessUserType.MAJOR.getValue())) {
                            if (Objects.equals(processTaskStepUserVo.getUserUuid(), userUuid)) {
                                //11.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                                //12.判断步骤状态是否是“处理中”，如果是，则提示“步骤处理中”；
                                exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                                        ProcessTaskStepStatus.RUNNING);
                                if (exception != null) {
                                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                            .put(operationType, exception);
                                    return false;
                                }
                            } else {
                                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepUserVo.getUserUuid());
                                if (userVo != null) {
                                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                            .put(operationType, new ProcessTaskStepHandledByOthersException(userVo.getUserId(), userVo.getUserName()));
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否是当前步骤的待处理人，如果不是，则提示“您不是步骤待处理人”；
            if (!checkIsWorker(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotWorkerException());
                return false;
            }
            //14.判断当前用户是否是当前步骤的处理人，如果是，则提示“您已经是步骤处理人”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_START, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_START;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //12.判断步骤状态是否是“处理中”，如果是，则提示“步骤处理中”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.RUNNING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMPLETE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMPLETE;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //3.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //4.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //5.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //6.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //7.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //8.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //9.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //10.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //11.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //12.判断当前步骤是否有下一步骤，如果没有，则提示“该步骤没有下一步骤”；
            if (!checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.FORWARD)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotNextStepException());
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            return true;
        });
        /**
         * 步骤回退权限
         * 判断userUuid用户是否有步骤回退权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人，且步骤有回退（虚线）方向的连线
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_BACK, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_BACK;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //13.判断当前步骤是否有回退线，如果没有，则提示“该步骤未启用回退功能”；
            if (!checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, processTaskStepVo.getId(), ProcessFlowDirection.BACKWARD)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepBackNotEnabledException());
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //14.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
            if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotMajorUserException());
                return false;
            }
            return true;
        });
        /**
         * 步骤暂存权限
         * 判断userUuid用户是否有步骤暂存权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_SAVE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_SAVE;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_COMMENT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            String processTaskStepEnableComment = ConfigManager.getConfig(ItsmTenantConfig.PROCESS_TASK_STEP_ENABLE_COMMENT);
            if (!Objects.equals(processTaskStepEnableComment, "1")) {
                return false;
            }
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMMENT;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_PAUSE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_PAUSE;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
            //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.HANG,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否有当前步骤“暂停”操作权限，如果没有，则提示“您的'暂停'操作未获得授权”；
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
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RECOVER, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
            Long id = processTaskStepVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RECOVER;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
            //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.SUCCEED,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
            if (processTaskStepVo.getIsActive() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotActiveException());
                return false;
            }
            //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
            //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
            //11.判断步骤状态是否是“处理中”，如果是，则提示“步骤处理中”；
            //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
            exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                    ProcessTaskStepStatus.FAILED,
                    ProcessTaskStepStatus.RUNNING,
                    ProcessTaskStepStatus.PENDING);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //13.判断当前用户是否有当前步骤“暂停”操作权限，如果没有，则提示“您的'恢复'操作未获得授权”；
            if (checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, ProcessTaskOperationType.STEP_PAUSE, userUuid)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                    .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
            return false;
        });
        /**
         * 步骤撤回权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“已完成”，然后userUuid用户在步骤权限设置中获得“撤回”的授权，当前步骤流转时激活步骤列表中有未完成的步骤
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_RETREAT,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_RETREAT;
                //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
                //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                //8.判断步骤状态是否是“已完成”，如果不是，则提示“步骤未完成”；
                if (!ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStepVo.getStatus())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepUndoneException());
                    return false;
                }
                //9.判断当前步骤的下一步骤是否已经完成，如果是，则提示“该步骤已经不能撤回”；
                if (!checkCurrentStepIsRetractableByProcessTaskStepId(processTaskVo, processTaskStepVo.getId())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepCannotRetreatException());
                    return false;
                }
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //10.判断当前用户是否有当前步骤“撤回”操作权限，如果没有，则提示“您的'撤回'操作未获得授权”；
                // 撤销权限retreat
                if (!checkOperationAuthIsConfigured(processTaskVo, processTaskStepVo, operationType, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                    return false;
                }
                return true;
            });
        /**
         * 步骤重审权限
         * 判断userUuid用户是否有步骤撤回权限逻辑：
         * 首先工单状态是“处理中”，步骤状态是“处理中”，然后userUuid用户是步骤处理人，当前步骤是由回退线操作激活的
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_REAPPROVAL,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_REAPPROVAL;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                    //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                    //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                    //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                    //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                    ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                    //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotActiveException());
                        return false;
                    }
                    //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                    //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
                    //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
                    //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
                    exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                            ProcessTaskStepStatus.FAILED,
                            ProcessTaskStepStatus.HANG,
                            ProcessTaskStepStatus.PENDING);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    //13.判断当前步骤是否启用重审功能，如果没有，则提示“该步骤未启用重审功能”；
                    if (!Objects.equals(processTaskStepVo.getEnableReapproval(), 1)){
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepReapprovalNotEnabledException());
                        return false;
                    }
                    //14.判断当前步骤有没有对应需要重审的步骤，如果没有，则提示“没有需要重审的步骤”；
                    boolean flag = false;
                    List<ProcessTaskStepRelVo> relList = processTaskVo.getStepRelList();
                    if (CollectionUtils.isNotEmpty(relList)) {
                        for (ProcessTaskStepRelVo processTaskStepRelVo : relList) {
                            if (Objects.equals(id, processTaskStepRelVo.getToProcessTaskStepId())) {
                                if (Objects.equals(processTaskStepRelVo.getType(), ProcessFlowDirection.BACKWARD.getValue()) && Objects.equals(processTaskStepRelVo.getIsHit(), 1)) {
                                    flag = true;
                                }
                            }
                        }
                    }
                    if (!flag) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNoNeedReapprovalStepException());
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //15.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
                    if (!checkIsProcessTaskStepUser(processTaskStepVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotMajorUserException());
                        return false;
                    }
                    return true;
                });
        /**
         * 步骤处理权限
         * 判断userUuid用户是否有步骤处理权限逻辑：
         * 首先步骤状态是“已激活”，然后userUuid用户是步骤的处理人或待处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.STEP_WORK,
            (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                Long id = processTaskStepVo.getId();
                ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_WORK;
                //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                if (processTaskVo.getIsShow() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskHiddenException());
                    return false;
                }
                //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
                if (processTaskStepVo.getIsActive() == 0) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskStepNotActiveException());
                    return false;
                }
                //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
                //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
                exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                        ProcessTaskStepStatus.FAILED,
                        ProcessTaskStepStatus.HANG);
                if (exception != null) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, exception);
                    return false;
                }
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //12.判断当前用户是否是当前步骤的待处理人，如果不是，则提示“您不是步骤待处理人”；
                if (checkIsWorker(processTaskStepVo, userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskStepNotWorkerException());
                return false;
            });

        /**
         * 步骤创建任务权限
         * 判断userUuid用户是否有步骤创建任务权限逻辑：
         * 首先步骤状态是“处理中”，然后userUuid用户是步骤的处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_CREATE,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.TASK_CREATE;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                    //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                    //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                    //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                    //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                    ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                    //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotActiveException());
                        return false;
                    }
                    //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                    //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
                    //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
                    //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
                    exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                            ProcessTaskStepStatus.FAILED,
                            ProcessTaskStepStatus.HANG,
                            ProcessTaskStepStatus.PENDING);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //13.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
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
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap, extraParam) -> {
                    Long id = processTaskStepVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.TASK_DELETE;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                    //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
                    //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                    //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                    //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                    ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
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
                    //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
                    if (processTaskStepVo.getIsActive() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskStepNotActiveException());
                        return false;
                    }
                    //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
                    //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
                    //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
                    exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStepStatus.SUCCEED,
                            ProcessTaskStepStatus.FAILED,
                            ProcessTaskStepStatus.HANG);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //12.判断当前用户是否是当前步骤的处理人，如果不是，则提示“您不是步骤处理人”；
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
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

}
