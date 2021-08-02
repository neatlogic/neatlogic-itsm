package codedriver.module.process.operationauth.handler;

import java.util.*;

import javax.annotation.PostConstruct;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONPath;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.module.process.service.CatalogService;

@Component
public class TaskOperateHandler extends OperationAuthHandlerBase {

    private Map<ProcessTaskOperationType,
            TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();
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
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_VIEW,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1 || AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                        if (!ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                            if (userUuid.equals(processTaskVo.getOwner())) {
                                return true;
                            } else if (userUuid.equals(processTaskVo.getReporter())) {
                                return true;
                            } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                                return true;
                            } else if (checkIsWorker(processTaskVo, userUuid)) {
                                return true;
                            } else {
                                return catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid);
                            }
                        }
                    }
                    return false;
                });
        /**
         * 工单提交权限
         * 判断userUuid用户是否有工单提交权限逻辑：
         * 首先工单状态是“未提交”，然后userUuid用户是上报人或代报人，则有工单提交权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_START,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1) {
                        if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                            if (userUuid.equals(processTaskVo.getOwner()) || userUuid.equals(processTaskVo.getReporter())) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
        /**
         * 工单取消权限
         * 判断userUuid用户是否有工单取消权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“取消”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_ABORT,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1) {
                        // 工单状态为进行中的才能终止
                        if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                            return checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_ABORT, userUuid);
                        }
                    }
                    return false;
                });
        /**
         * 工单恢复权限
         * 判断userUuid用户是否有工单恢复权限逻辑：
         * 首先工单状态是“已取消”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“取消”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_RECOVER,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1) {
                        // 工单状态为已终止的才能恢复
                        if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                            return checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_ABORT, userUuid);
                        }
                    }
                    return false;
                });
        /**
         * 工单修改上报内容（包括标题、优先级、描述）权限
         * 判断userUuid用户是否有工单修改上报内容权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“修改上报内容”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_UPDATE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    return checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_UPDATE, userUuid);
                }
            }
            return false;
        });
        /**
         * 工单催单权限
         * 判断userUuid用户是否有工单催单权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户在工单对应流程图的流程设置-权限设置中获得“催单”的授权
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_URGE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    return checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_URGE, userUuid);
                }
            }
            return false;
        });
        /**
         * 工单处理权限
         * 判断userUuid用户是否有工单处理权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户是工单中某个步骤的处理人或协助处理人
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_WORK, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    // 有可处理步骤work
                    return checkIsWorker(processTaskVo, userUuid);
                }
            }
            return false;
        });
        /**
         * 工单撤回权限
         * 判断userUuid用户是否有工单撤回权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户拥有工单中某个步骤的撤回权限，则有工单撤回权限
         * 步骤撤回权限逻辑在{@link codedriver.module.process.operationauth.handler.StepOperateHandler#init}中的ProcessTaskOperationType.STEP_RETREAT里
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_RETREAT, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                // 撤销权限retreat
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                        if (processTaskStep.getIsActive().intValue() == 1) {
                            return checkIsRetractableStepByProcessTaskStepId(processTaskVo, processTaskStep.getId(), userUuid);
                        }
                    }
                }
            }
            return false;
        });
        /**
         * 工单评分权限
         * 判断userUuid用户是否有工单评分权限逻辑：
         * 首先工单状态是“已完成”，然后userUuid用户是工单上报人，且在工单对应流程图的评分设置中启用评分
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_SCORE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                // 评分权限score
                if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        String taskConfig = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                        Integer isActive = (Integer) JSONPath.read(taskConfig, "process.scoreConfig.isActive");
                        return Objects.equals(isActive, 1);
                    }
                }
            }
            return false;
        });
        /**
         * 工单转报权限
         * 判断userUuid用户是否有工单转报权限逻辑：
         * userUuid用户在工单对应服务的转报设置中获得授权，且对转报服务有上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_TRANFERREPORT,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1) {
                        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                        List<String> userRoleUuidList = roleMapper.getRoleUuidListByUserUuid(userUuid);
                        List<String> teamRoleUuidList = roleMapper.getRoleUuidListByTeamUuidList(teamUuidList);
                        Set<String> roleUuidSet = new HashSet<>();
                        roleUuidSet.addAll(userRoleUuidList);
                        roleUuidSet.addAll(teamRoleUuidList);
                        List<String> roleUuidList = new ArrayList<>(roleUuidSet);
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
                                processTaskVo.getChannelUuid(), userUuid, teamUuidList, roleUuidList, processUserTypeList);
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
                    }
                    return false;
                });
        /**
         * 工单复杂上报权限
         * 判断userUuid用户是否有工单复杂上报权限逻辑：
         * userUuid用户有当前工单对应服务的上报权限，则有工单复杂上报权限
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_COPYPROCESSTASK,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1) {
                        return catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid);
                    }
                    return false;
                });
        /**
         * 工单重做权限
         * 判断userUuid用户是否有工单重做权限逻辑：
         * 首先工单状态是“已完成”，然后userUuid用户是工单上报人，且在工单对应流程图的评分设置-评分前允许回退中设置了回退步骤列表
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_REDO, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                    if (userUuid.equals(processTaskVo.getOwner())) {
                        for(ProcessTaskStepVo stepVo : processTaskVo.getStepList()){
                            if(stepVo.getType().equals(ProcessStepType.END.getValue())){
                                return checkNextStepIsExistsByProcessTaskStepIdAndProcessFlowDirection(processTaskVo, stepVo.getId(), ProcessFlowDirection.BACKWARD);
                            }
                        }
                    }
                }
            }
            return false;
        });
        /**
         * 工单转交权限
         * 判断userUuid用户是否有工单转交权限逻辑：
         * 首先工单状态是“处理中”，然后userUuid用户拥有工单中某个步骤的转交权限，则有工单转交权限
         * 步骤转交权限逻辑在{@link codedriver.module.process.operationauth.handler.StepOperateHandler#init}中的ProcessTaskOperationType.STEP_TRANSFER里
         */
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_TRANSFER, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                        if (processTaskStep.getIsActive().intValue() == 1) {
                            return checkOperationAuthIsConfigured(processTaskVo, processTaskStep, ProcessTaskOperationType.STEP_TRANSFER, userUuid);
                        }
                    }
                }
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_SHOW, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 0) {
                return AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName());
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_HIDE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (processTaskVo.getIsShow() == 1) {
                return AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName());
            }
            return false;
        });
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_DELETE, (processTaskVo, processTaskStepVo, userUuid) -> {
            return AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName());
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
        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_FOCUSUSER_UPDATE,
                (processTaskVo, processTaskStepVo, userUuid) -> {
                    if (processTaskVo.getIsShow() == 1 || AuthActionChecker.checkByUserUuid(userUuid, PROCESSTASK_MODIFY.class.getSimpleName())) {
                        if (!ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                            if (userUuid.equals(processTaskVo.getOwner())) {
                                return true;
                            } else if (userUuid.equals(processTaskVo.getReporter())) {
                                return true;
                            } else if (checkIsProcessTaskStepUser(processTaskVo, userUuid)) {
                                return true;
                            } else if (checkIsWorker(processTaskVo, userUuid)) {
                                return true;
                            } else {
                               return false;
                            }
                        }
                    }
                    return false;
                });
    }

    @Override
    public String getHandler() {
        return OperationAuthHandlerType.TASK.getValue();
    }

    @Override
    public Map<ProcessTaskOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>>
    getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }
}
