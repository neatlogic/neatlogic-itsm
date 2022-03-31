package codedriver.module.process.operationauth.handler;

import java.util.*;

import javax.annotation.PostConstruct;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.operationauth.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONPath;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.module.process.service.CatalogService;

@Component
public class TaskOperateHandler extends OperationAuthHandlerBase {

    private Map<ProcessTaskOperationType,
            TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ChannelTypeMapper channelTypeMapper;
    @Autowired
    private CatalogMapper catalogMapper;
    @Autowired
    private CatalogService catalogService;

    @PostConstruct
    public void init() {
        /**
         * 工单查看权限
         * 判断userUuid用户是否有工单查看权限逻辑：
         * 首先工单状态不是“未提交”，
         * 符合一下几种情况之一就有工单查看权限：
         * 1.userUuid用户是上报人
         * 2.userUuid用户是代报人
         * 3.userUuid用户是工单中某个“已完成”步骤的处理人或协助处理人
         * 4.userUuid用户是工单中某个“处理中”步骤的处理人或协助处理人
         * 5.userUuid用户有当前工单对应服务的上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_VIEW,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_VIEW;
                    //1.判断工单是否被隐藏
                    if (processTaskVo.getIsShow() == 0) {
                        //判断当前用户是否有“工单管理权限”或者是系统用户，如果两者都没有，则提示“工单已隐藏”；
                        if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName()) && !SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                    .put(operationType, new ProcessTaskHiddenException());
                            return false;
                        }
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(), ProcessTaskStatus.DRAFT);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //4.依次判断当前用户是否是工单上报人、代报人、处理人、待处理人，如果都不是，执行第5步；
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        return true;
                    } else if (userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                        return true;
                    } else if (checkIsWorker(processTaskVo, userUuid)) {
                        return true;
                    }
                    //5.判断当前用户是否有工单对应服务的上报权限，如果没有，则提示“您没有【xxx】服务的上报权限”；
                    if (catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid)) {
                        return true;
                    }
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNotChannelReportException(channelVo.getName()));
                    return false;
                });
        /**
         * 工单提交权限
         * 判断userUuid用户是否有工单提交权限逻辑：
         * 首先工单状态是“未提交”，然后userUuid用户是上报人或代报人，则有工单提交权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_START,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_START;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果不是，则提示“工单已提交”；
                    if (!ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskSubmittedException());
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //3.依次判断当前用户是否是工单上报人、代报人，如果都不是，则提示“您不是工单上报人或代报人”；
                    if (userUuid.equals(processTaskVo.getOwner()) || userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNotOwnerException());
                    return false;
                });
        /**
         * 工单取消权限
         * 判断userUuid用户是否有工单取消权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“取消”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_ABORT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_ABORT;
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
                    // 工单状态为进行中的才能终止
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                        //系统用户默认拥有权限
                        if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                            return true;
                        }
                        //8.判断当前用户是否有“取消”操作权限，如果没有，则提示“您的'取消'操作未获得授权”；
                        if (!checkOperationAuthIsConfigured(processTaskVo, operationType, userUuid)) {
                            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                    .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                            return false;
                        }
                        return true;
                    }
                    return false;
                });
        /**
         * 工单恢复权限
         * 判断userUuid用户是否有工单恢复权限逻辑：
         * 首先工单状态是“已取消”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“取消”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_RECOVER,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_RECOVER;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
                    //4.判断工单状态是否是“处理中”，如果是，则提示“工单处理中”；
                    //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
                    //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
                    //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
                    ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.RUNNING,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED);
                    if (exception != null) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, exception);
                        return false;
                    }
                    // 工单状态为已终止的才能恢复
                    if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                        //系统用户默认拥有权限
                        if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                            return true;
                        }
                        //8.判断当前用户是否有“取消”操作权限，如果没有，则提示“您的'恢复'操作未获得授权”；
                        if (!checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_ABORT, userUuid)) {
                            operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                    .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                            return false;
                        }
                        return true;
                    }
                    return false;
                });
        /**
         * 工单修改上报内容（包括标题、优先级、描述）权限
         * 判断userUuid用户是否有工单修改上报内容权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“修改上报内容”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_UPDATE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_UPDATE;
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //8.判断当前用户是否有“修改上报内容”操作权限，如果没有，则提示“您的'修改上报内容'操作未获得授权”；
                if (!checkOperationAuthIsConfigured(processTaskVo, operationType, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                    return false;
                }
                return true;
            }
            return false;
        });
        /**
         * 工单催单权限
         * 判断userUuid用户是否有工单催单权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“催单”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_URGE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_URGE;
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //8.判断当前用户是否有“催办”操作权限，如果没有，则提示“您的'催办'操作未获得授权”；
                if (!checkOperationAuthIsConfigured(processTaskVo, operationType, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                    return false;
                }
                return true;
            }
            return false;
        });
        /**
         * 工单处理权限
         * 判断userUuid用户是否有工单处理权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户是工单中某个步骤的处理人或协助处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_WORK, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_WORK;
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //8.判断当前用户是否是工单某个步骤的待处理人，如果不是，则提示“工单里没有您可以处理的步骤”；
                // 有可处理步骤work
                if (checkIsWorker(processTaskVo, userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNoProcessableStepsException());
                return false;
            }
            return false;
        });
        /**
         * 工单撤回权限
         * 判断userUuid用户是否有工单撤回权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户拥有工单中某个步骤的撤回权限，则有工单撤回权限
         * 步骤撤回权限逻辑在{@link codedriver.module.process.operationauth.handler.StepOperateHandler#init}中的ProcessTaskOperationType.STEP_RETREAT里
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_RETREAT, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_RETREAT;
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
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //8.判断当前用户是否有工单某个步骤的撤回权限，如果没有，则提示“工单里没有您可以撤回的步骤”；
                boolean flag = false;
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        flag = checkIsRetractableStepByProcessTaskStepId(processTaskVo, processTaskStep.getId(), userUuid);
                        if (flag) {
                            return true;
                        }
                    }
                }
                if (!flag) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNoRetreatableStepsException());
                    return false;
                }
            }
            return false;
        });
        /**
         * 工单评分权限
         * 判断userUuid用户是否有工单评分权限逻辑：
         * 首先工单状态是“已完成”，然后userUuid用户是工单上报人，且在工单对应流程图的评分设置中启用评分
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_SCORE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_SCORE;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“已完成”，如果不是，则提示“工单未完成”；
            if (!ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskUndoneException());
                return false;
            }
            //3.判断工单是否启用“评分”功能，如果没有，则提示“工单未启用评分功能”；
            Integer isActive = (Integer) JSONPath.read(processTaskVo.getConfig(), "process.scoreConfig.isActive");
            if (!Objects.equals(isActive, 1)) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskScoreNotEnabledException());
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //4.依次判断当前用户是否是工单上报人、代报人，如果都不是，则提示“您不是工单上报人或代报人”；
            // 评分权限score
            if (!userUuid.equals(processTaskVo.getOwner()) && !userUuid.equals(processTaskVo.getReporter())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNotOwnerException());
                return false;
            }
            return true;
        });
        /**
         * 工单转报权限
         * 判断userUuid用户是否有工单转报权限逻辑：
         * userUuid用户在工单对应服务的转报设置中获得授权，且对转报服务有上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单对应的服务是否启用转报功能，如果没有，则提示“工单对应的服务【"xxx"】未启用转报功能”；
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    JSONObject config = channelVo.getConfig();
                    Integer allowTranferReport = config.getInteger("allowTranferReport");
                    if (!Objects.equals(allowTranferReport, 1)) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskChannelTranferReportNotEnabledException(channelVo.getName()));
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //3.判断当前用户是否有“转报”操作权限，如果没有，则提示“您的'转报'操作未获得授权”；
                    AuthenticationInfoVo authenticationInfoVo = null;
                    if (Objects.equals(UserContext.get().getUserUuid(), userUuid)) {
                        authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
                    } else {
                        authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
                    }
                    List<String> processUserTypeList = new ArrayList<>();
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        processUserTypeList.add(ProcessUserType.OWNER.getValue());
                    }
                    if (userUuid.equals(processTaskVo.getReporter())) {
                        processUserTypeList.add(ProcessUserType.REPORTER.getValue());
                    }
                    if (checkIsProcessTaskStepUser(processTaskVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        processUserTypeList.add(ProcessUserType.MAJOR.getValue());
                        processUserTypeList.add(ProcessUserType.WORKER.getValue());
                    } else if (checkIsWorker(processTaskVo, ProcessUserType.MAJOR.getValue(), userUuid)) {
                        processUserTypeList.add(ProcessUserType.WORKER.getValue());
                    }
                    if (checkIsProcessTaskStepUser(processTaskVo, ProcessUserType.MINOR.getValue(), userUuid)) {
                        processUserTypeList.add(ProcessUserType.MINOR.getValue());
                    }
                    List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(
                            processTaskVo.getChannelUuid(), userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), processUserTypeList);
                    if (CollectionUtils.isNotEmpty(channelTypeRelationIdList)) {
                        ChannelRelationVo channelRelationVo = new ChannelRelationVo();
                        channelRelationVo.setSource(processTaskVo.getChannelUuid());
                        for (Long channelTypeRelationId : channelTypeRelationIdList) {
                            channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
                            List<ChannelRelationVo> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
                            if (CollectionUtils.isNotEmpty(channelRelationTargetList)) {
                                List<String> channelTypeUuidList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
                                if (channelTypeUuidList.contains("all")) {
                                    channelTypeUuidList.clear();
                                }
                                for (ChannelRelationVo channelRelation : channelRelationTargetList) {
                                    if ("channel".equals(channelRelation.getType())) {
                                        return true;
                                    } else if ("catalog".equals(channelRelation.getType())) {
                                        if (channelTypeMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(channelRelation.getTarget(), channelTypeUuidList) > 0) {
                                            return true;
                                        } else {
                                            CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelRelation.getTarget());
                                            if (catalogVo != null) {
                                                List<String> uuidList = catalogMapper.getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                                                for (String uuid : uuidList) {
                                                    if (!channelRelation.getTarget().equals(uuid)) {
                                                        if (channelTypeMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(uuid, channelTypeUuidList) > 0) {
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
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskOperationUnauthorizedException(operationType));
                    return false;
                });

        /**
         * 工单标记重复事件权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_MARKREPEAT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_MARKREPEAT;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单是否启用“标记重复事件”功能，如果没有，则提示“工单未启用标记重复事件功能”；
                    Integer enableMarkRepeat = (Integer) JSONPath.read(processTaskVo.getConfig(), "process.processConfig.enableMarkRepeat");
                    if (Objects.equals(enableMarkRepeat, 1)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskMarkRepeatNotEnabledException());
                    return false;
                });
        /**
         * 工单复制上报权限
         * 判断userUuid用户是否有工单复杂上报权限逻辑：
         * userUuid用户有当前工单对应服务的上报权限，则有工单复杂上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //2.判断当前用户是否有工单对应服务的上报权限，如果没有，则提示“您没有【xxx】服务的上报权限”；
                    if (catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid)) {
                        return true;
                    }
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNotChannelReportException(channelVo.getName()));
                    return false;
                });
        /**
         * 工单重做权限
         * 判断userUuid用户是否有工单重做权限逻辑：
         * 首先工单状态是“已完成”，然后userUuid用户是工单上报人，且在工单对应流程图的评分设置-评分前允许回退中设置了回退步骤列表
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_REDO, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_REDO;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
            //4.判断工单状态是否是“处理中”，如果是，则提示“工单处理中”；
            //3.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
            //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
            //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
            //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
            ProcessTaskPermissionDeniedException exception = checkProcessTaskStatus(processTaskVo.getStatus(),
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.RUNNING,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED);
            if (exception != null) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, exception);
                return false;
            }
            if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                //8.判断工单是否启用“重做”功能，如果没有，则提示“工单未启用重做功能”；
                boolean flag = true;
                for(ProcessTaskStepVo stepVo : processTaskVo.getStepList()){
                    if(stepVo.getType().equals(ProcessStepType.END.getValue())){
                        flag = checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, stepVo.getId(), ProcessFlowDirection.BACKWARD);
                        break;
                    }
                }
                if (!flag) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskBackNotEnabledException());
                    return false;
                }
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //9.依次判断当前用户是否是工单上报人、代报人，如果都不是，则提示“您不是工单上报人或代报人”；
                if (!userUuid.equals(processTaskVo.getOwner()) && !userUuid.equals(processTaskVo.getReporter())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNotOwnerException());
                    return false;
                }
                return true;
            }
            return false;
        });
        /**
         * 工单转交权限
         * 判断userUuid用户是否有工单转交权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户拥有工单中某个步骤的转交权限，则有工单转交权限
         * 步骤转交权限逻辑在{@link codedriver.module.process.operationauth.handler.StepOperateHandler#init}中的ProcessTaskOperationType.STEP_TRANSFER里
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_TRANSFER, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_TRANSFER;
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                //系统用户默认拥有权限
                if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                    return true;
                }
                //8.判断当前用户是否有工单某个步骤的转交权限，如果没有，则提示“工单里没有您可以转交的步骤”；
                boolean flag = false;
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        flag = checkOperationAuthIsConfigured(processTaskVo, processTaskStep, ProcessTaskOperationType.STEP_TRANSFER, userUuid);
                        if (flag) {
                            return true;
                        }
                    }
                }
                if (!flag) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNoTransferableStepsException());
                    return false;
                }
            }
            return false;
        });

        /**
         * 工单显示权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_SHOW, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_SHOW;
            //1.判断工单是否被显示，如果isShow=1，则提示“工单已显示”；
            if (processTaskVo.getIsShow() == 1) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskShownException());
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //2.判断当前用户是否有“工单管理权限”，如果没有，则提示“没有工单管理权限”；
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            return true;
        });
        /**
         * 工单隐藏权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_HIDE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_HIDE;
            //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskHiddenException());
                return false;
            }
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            //2.判断当前用户是否有“工单管理权限”，如果没有，则提示“没有工单管理权限”；
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            return true;
        });
        /**
         * 工单删除权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_DELETE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            //系统用户默认拥有权限
            if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                return true;
            }
            Long id = processTaskVo.getId();
            ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_DELETE;
            //2.判断当前用户是否有“工单管理权限”，如果没有，则提示“没有工单管理权限”；
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                        .put(operationType, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            return true;
        });
        /**
         * 修改工单关注人权限
         * 首先工单状态不是“未提交”，
         * 符合一下几种情况之一就有修改工单关注人权限：
         * 1.userUuid用户是上报人
         * 2.userUuid用户是代报人
         * 3.userUuid用户是工单中某个“已完成”步骤的处理人或协助处理人
         * 4.userUuid用户是工单中某个“处理中”步骤的处理人或协助处理人
        **/
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    Long id = processTaskVo.getId();
                    ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE;
                    //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskHiddenException());
                        return false;
                    }
                    //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
                    if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskUnsubmittedException());
                        return false;
                    }
                    //系统用户默认拥有权限
                    if (SystemUser.SYSTEM.getUserUuid().equals(userUuid)) {
                        return true;
                    }
                    //3.判断当前用户是否有“工单管理权限”，如果没有，则提示“没有工单管理权限”；
                    if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                                .put(operationType, new ProcessTaskNotProcessTaskModifyException());
                        return false;
                    }
                    //4.依次判断当前用户是否是工单上报人、代报人、处理人、待处理人，如果都不是，则提示“您不是工单干系人”；
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        return true;
                    } else if (userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                        return true;
                    } else if (checkIsWorker(processTaskVo, userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(id, key -> new HashMap<>())
                            .put(operationType, new ProcessTaskNotProcessUserException());
                    return false;
                });
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.TASK.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<ProcessTaskOperationType, ProcessTaskPermissionDeniedException>>>>
    getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }
}
