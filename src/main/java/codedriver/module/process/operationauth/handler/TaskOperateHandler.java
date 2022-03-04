package codedriver.module.process.operationauth.handler;

import java.util.*;

import javax.annotation.PostConstruct;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.core.ProcessTaskException;
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
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_VIEW, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_VIEW, new ProcessTaskNotProcessTaskModifyException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskVo.getId(),
                            ProcessTaskOperationType.PROCESSTASK_VIEW,
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
                    } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                        return true;
                    } else if (checkIsWorker(processTaskVo, userUuid)) {
                        return true;
                    }
                    if (catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid)) {
                        return true;
                    }
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_VIEW, new ProcessTaskNotChannelReportException(channelVo.getName()));
                    return false;
                });
        /**
         * 工单提交权限
         * 判断userUuid用户是否有工单提交权限逻辑：
         * 首先工单状态是“未提交”，然后userUuid用户是上报人或代报人，则有工单提交权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_START,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_START, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (!ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_START, new ProcessTaskSubmittedException());
                        return false;
                    }
                    if (userUuid.equals(processTaskVo.getOwner()) || userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_START, new ProcessTaskNotOwnerException());
                    return false;
                });
        /**
         * 工单取消权限
         * 判断userUuid用户是否有工单取消权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“取消”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_ABORT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_ABORT, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskVo.getId(),
                            ProcessTaskOperationType.PROCESSTASK_ABORT,
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
                    // 工单状态为进行中的才能终止
                    if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                        if (!checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_ABORT, userUuid)) {
                            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                    .put(ProcessTaskOperationType.PROCESSTASK_ABORT, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_ABORT));
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
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_RECOVER, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (checkProcessTaskStatus(processTaskVo.getId(),
                            ProcessTaskOperationType.PROCESSTASK_RECOVER,
                            processTaskVo.getStatus(),
                            operationTypePermissionDeniedExceptionMap,
                            ProcessTaskStatus.DRAFT,
                            ProcessTaskStatus.SUCCEED,
                            ProcessTaskStatus.RUNNING,
                            ProcessTaskStatus.FAILED,
                            ProcessTaskStatus.HANG,
                            ProcessTaskStatus.SCORED
                    )) {
                        return false;
                    }
                    // 工单状态为已终止的才能恢复
                    if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                        if (!checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_ABORT, userUuid)) {
                            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                    .put(ProcessTaskOperationType.PROCESSTASK_RECOVER, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_RECOVER));
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_UPDATE, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_UPDATE,
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if (!checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_UPDATE, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_UPDATE, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_UPDATE));
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_URGE, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_URGE,
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if (!checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_URGE, userUuid)) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_URGE, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_URGE));
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_WORK, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_WORK,
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                // 有可处理步骤work
                if (checkIsWorker(processTaskVo, userUuid)) {
                    return true;
                }
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_WORK, new ProcessTaskNoProcessableStepsException());
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_RETREAT, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_RETREAT,
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
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
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
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_RETREAT, new ProcessTaskNoRetreatableStepsException());
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_SCORE, new ProcessTaskHiddenException());
                return false;
            }
            if (!ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_SCORE, new ProcessTaskUndoneException());
                return false;
            }
            // 评分权限score
            if (!userUuid.equals(processTaskVo.getOwner()) && !userUuid.equals(processTaskVo.getReporter())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_SCORE, new ProcessTaskNotOwnerException());
                return false;
            }
            Integer isActive = (Integer) JSONPath.read(processTaskVo.getConfig(), "process.scoreConfig.isActive");
            if (Objects.equals(isActive, 1)) {
                return true;
            }
            operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                    .put(ProcessTaskOperationType.PROCESSTASK_SCORE, new ProcessTaskScoreNotEnabledException());
            return false;
        });
        /**
         * 工单转报权限
         * 判断userUuid用户是否有工单转报权限逻辑：
         * userUuid用户在工单对应服务的转报设置中获得授权，且对转报服务有上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT, new ProcessTaskHiddenException());
                        return false;
                    }
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    JSONObject config = channelVo.getConfig();
                    Integer allowTranferReport = config.getInteger("allowTranferReport");
                    if (!Objects.equals(allowTranferReport, 1)) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT, new ProcessTaskChannelTranferReportNotEnabledException(channelVo.getName()));
                        return false;
                    }
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
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT, new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_TRANFERREPORT));
                    return false;
                });

        /**
         * 工单标记重复事件权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_MARKREPEAT,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_MARKREPEAT, new ProcessTaskHiddenException());
                        return false;
                    }
                    Integer enableMarkRepeat = (Integer) JSONPath.read(processTaskVo.getConfig(), "process.processConfig.enableMarkRepeat");
                    if (Objects.equals(enableMarkRepeat, 1)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_MARKREPEAT, new ProcessTaskMarkRepeatNotEnabledException());
                    return false;
                });
        /**
         * 工单复制上报权限
         * 判断userUuid用户是否有工单复杂上报权限逻辑：
         * userUuid用户有当前工单对应服务的上报权限，则有工单复杂上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK,
                (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid)) {
                        return true;
                    }
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK, new ProcessTaskNotChannelReportException(channelVo.getName()));
                    return false;
                });
        /**
         * 工单重做权限
         * 判断userUuid用户是否有工单重做权限逻辑：
         * 首先工单状态是“已完成”，然后userUuid用户是工单上报人，且在工单对应流程图的评分设置-评分前允许回退中设置了回退步骤列表
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_REDO, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_REDO, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_REDO,
                    processTaskVo.getStatus(),
                    operationTypePermissionDeniedExceptionMap,
                    ProcessTaskStatus.DRAFT,
                    ProcessTaskStatus.RUNNING,
                    ProcessTaskStatus.ABORTED,
                    ProcessTaskStatus.FAILED,
                    ProcessTaskStatus.HANG,
                    ProcessTaskStatus.SCORED
            )) {
                return false;
            }
            if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                if (!userUuid.equals(processTaskVo.getOwner()) && !userUuid.equals(processTaskVo.getReporter())) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_REDO, new ProcessTaskNotOwnerException());
                    return false;
                }
                boolean flag = true;
                for(ProcessTaskStepVo stepVo : processTaskVo.getStepList()){
                    if(stepVo.getType().equals(ProcessStepType.END.getValue())){
                        flag = checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, stepVo.getId(), ProcessFlowDirection.BACKWARD);
                        break;
                    }
                }
                if (!flag) {
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_REDO, new ProcessTaskBackNotEnabledException());
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
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_TRANSFER, new ProcessTaskHiddenException());
                return false;
            }
            if (checkProcessTaskStatus(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_TRANSFER,
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
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
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
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_TRANSFER, new ProcessTaskNoTransferableStepsException());
                    return false;
                }
            }
            return false;
        });

        /**
         * 工单显示权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_SHOW, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 1) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_SHOW, new ProcessTaskShownException());
                return false;
            }
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_HIDE, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            return true;
        });
        /**
         * 工单隐藏权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_HIDE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (processTaskVo.getIsShow() == 0) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_HIDE, new ProcessTaskHiddenException());
                return false;
            }
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_HIDE, new ProcessTaskNotProcessTaskModifyException());
                return false;
            }
            return true;
        });
        /**
         * 工单删除权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.PROCESSTASK_DELETE, (processTaskVo, processTaskStepVo, userUuid, operationTypePermissionDeniedExceptionMap) -> {
            if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                        .put(ProcessTaskOperationType.PROCESSTASK_DELETE, new ProcessTaskNotProcessTaskModifyException());
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
                    if (processTaskVo.getIsShow() == 0) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE, new ProcessTaskHiddenException());
                        return false;
                    }
                    if (!AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE, new ProcessTaskNotProcessTaskModifyException());
                        return false;
                    }
                    if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                        operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                                .put(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE, new ProcessTaskUnsubmittedException());
                        return false;
                    }
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        return true;
                    } else if (userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                        return true;
                    } else if (checkIsWorker(processTaskVo, userUuid)) {
                        return true;
                    }
                    operationTypePermissionDeniedExceptionMap.computeIfAbsent(processTaskVo.getId(), key -> new HashMap<>())
                            .put(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE, new ProcessTaskNotProcessUserException());
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
