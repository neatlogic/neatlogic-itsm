package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.dto.ProcessTaskScoreTemplateVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.TernaryPredicate;
import codedriver.module.process.service.CatalogService;

@Component
public class TaskOperateHandler extends OperationAuthHandlerBase {

    private Map<ProcessTaskOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private CatalogMapper catalogMapper;
    @Autowired
    private CatalogService catalogService;
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @PostConstruct
    public void init() {

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_VIEW,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if (userUuid.equals(processTaskVo.getOwner())) {
                    return true;
                } else if (userUuid.equals(processTaskVo.getReporter())) {
                    return true;
                } else {
                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                    List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
                    List<String> channelList = channelMapper.getAuthorizedChannelUuidList(userUuid, teamUuidList,
                        roleUuidList, processTaskVo.getChannelUuid());
                    if (channelList.contains(processTaskVo.getChannelUuid())) {
                        return true;
                    } else if (checkIsWorker(processTaskVo, userUuid)) {
                        return true;
                    }
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_START,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                    if (userUuid.equals(processTaskVo.getOwner()) || userUuid.equals(processTaskVo.getReporter())) {
                        return true;
                    }
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_ABORT,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                // 工单状态为进行中的才能终止
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                    if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_ABORT,
                        userUuid)) {
                        return true;
                    }
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_RECOVER,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                // 工单状态为已终止的才能恢复
                if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                    if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_ABORT,
                        userUuid)) {
                        return true;
                    }
                }
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_UPDATE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_UPDATE, userUuid)) {
                    return true;
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_URGE, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.TASK_URGE, userUuid)) {
                    return true;
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_WORK, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 有可处理步骤work
            if (checkIsWorker(processTaskVo, userUuid)) {
                return true;
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_RETREAT, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        if (checkIsRetractableStepByProcessTaskStepId(processTaskVo, processTaskStep.getId(),
                            userUuid)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_SCORE, (processTaskVo, processTaskStepVo, userUuid) -> {
            // 评分权限score
            if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                if (userUuid.equals(processTaskVo.getOwner())) {
                    return true;
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_TRANFERREPORT,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
                List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
                List<Long> channelTypeRelationIdList =
                    channelMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(
                        processTaskVo.getChannelUuid(), userUuid, teamUuidList, roleUuidList);
                if (CollectionUtils.isNotEmpty(channelTypeRelationIdList)) {
                    ChannelRelationVo channelRelationVo = new ChannelRelationVo();
                    channelRelationVo.setSource(processTaskVo.getChannelUuid());
                    for (Long channelTypeRelationId : channelTypeRelationIdList) {
                        channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
                        List<ChannelRelationVo> channelRelationTargetList =
                            channelMapper.getChannelRelationTargetList(channelRelationVo);
                        if (CollectionUtils.isNotEmpty(channelRelationTargetList)) {
                            List<String> channelTypeUuidList = channelMapper
                                .getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
                            if (channelTypeUuidList.contains("all")) {
                                channelTypeUuidList.clear();
                            }
                            for (ChannelRelationVo channelRelation : channelRelationTargetList) {
                                if ("channel".equals(channelRelation.getType())) {
                                    return true;
                                } else if ("catalog".equals(channelRelation.getType())) {
                                    if (channelMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(
                                        channelRelation.getTarget(), channelTypeUuidList) > 0) {
                                        return true;
                                    } else {
                                        CatalogVo catalogVo =
                                            catalogMapper.getCatalogByUuid(channelRelation.getTarget());
                                        if (catalogVo != null) {
                                            List<String> uuidList = catalogMapper
                                                .getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                                            for (String uuid : uuidList) {
                                                if (!channelRelation.getTarget().equals(uuid)) {
                                                    if (channelMapper
                                                        .getActiveChannelCountByParentUuidAndChannelTypeUuidList(
                                                            channelRelation.getTarget(), channelTypeUuidList) > 0) {
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
                return false;
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_COPYPROCESSTASK,
            (processTaskVo, processTaskStepVo, userUuid) -> {
                if (catalogService.channelIsAuthority(processTaskVo.getChannelUuid())) {
                    return true;
                } else {
                    return false;
                }
            });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_REDO, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                ProcessTaskScoreTemplateVo processTaskScoreTemplateVo =
                    processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskVo.getId());
                if (processTaskScoreTemplateVo != null) {
                    if (StringUtils.isNotBlank(processTaskScoreTemplateVo.getConfigHash())) {
                        String configStr = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(
                            processTaskScoreTemplateVo.getConfigHash());
                        if (StringUtils.isNotBlank(configStr)) {
                            processTaskScoreTemplateVo.setConfig(configStr);
                            if (CollectionUtils
                                .isNotEmpty(processTaskScoreTemplateVo.getConfig().getJSONArray("stepUuidList"))) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.TASK_TRANSFER, (processTaskVo, processTaskStepVo, userUuid) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        if (checkOperationAuthIsConfigured(processTaskVo, processTaskStep,
                            ProcessTaskOperationType.STEP_TRANSFER, userUuid)) {
                            return true;
                        }
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
