package codedriver.module.process.operationauth.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
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
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class TaskOperateHandler implements IOperationAuthHandler {

    private Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private CatalogMapper catalogMapper;
    @Autowired
    private CatalogService catalogService;
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.POCESSTASKVIEW, (processTaskVo, processTaskStepVo) -> {
            if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
                return true;
            } else if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                return true;
            } else {
                List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                List<String> channelList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList(), processTaskVo.getChannelUuid());
                if (channelList.contains(processTaskVo.getChannelUuid())) {
                    return true;
                } else if(processTaskMapper.checkIsWorker(processTaskVo.getId(), null, null, UserContext.get().getUserUuid(), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.STARTPROCESS, (processTaskVo, processTaskStepVo) -> {
            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner()) || UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.ABORTPROCESSTASK, (processTaskVo, processTaskStepVo) -> {
            // 工单状态为进行中的才能终止
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(processTaskService.checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    return true;
                }
            }          
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RECOVERPROCESSTASK, (processTaskVo, processTaskStepVo) -> {
            // 工单状态为已终止的才能恢复
            if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskVo.getStatus())) {
                if(processTaskService.checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.UPDATE, (processTaskVo, processTaskStepVo) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(processTaskService.checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    return true;
                }
            }           
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.URGE, (processTaskVo, processTaskStepVo) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(processTaskService.checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    return true;
                }
            }            
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.WORK, (processTaskVo, processTaskStepVo) -> {
            List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
            // 有可处理步骤work
            if(processTaskMapper.checkIsWorker(processTaskVo.getId(), null, null, UserContext.get().getUserUuid(), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                return true;
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.RETREAT, (processTaskVo, processTaskStepVo) -> {
            // 撤销权限retreat
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    processTaskVo.getStepList().addAll(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId()));
                }
                Set<ProcessTaskStepVo> retractableStepSet = new HashSet<>();
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        retractableStepSet.addAll(processTaskService.getRetractableStepListByProcessTaskStepId(processTaskVo, processTaskStep.getId()));
                    }
                }
                if (CollectionUtils.isNotEmpty(retractableStepSet)) {
                    return true;
                }
            }
            return false;
        });

        operationBiPredicateMap.put(ProcessTaskOperationType.SCORE, (processTaskVo, processTaskStepVo) -> {
            // 评分权限score
            if(ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                String userUuid = UserContext.get().getUserUuid();
                if (userUuid.equals(processTaskVo.getOwner())) {
                    return true;
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.TRANFERREPORT, (processTaskVo, processTaskStepVo) -> {
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
            List<Long> channelTypeRelationIdList = channelMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(processTaskVo.getChannelUuid(), UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList());
            if(CollectionUtils.isNotEmpty(channelTypeRelationIdList)) {
                ChannelRelationVo channelRelationVo = new ChannelRelationVo();
                channelRelationVo.setSource(processTaskVo.getChannelUuid());
                for(Long channelTypeRelationId : channelTypeRelationIdList) {
                    channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
                    List<ChannelRelationVo> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
                    if(CollectionUtils.isNotEmpty(channelRelationTargetList)) {
                        List<String> channelTypeUuidList = channelMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
                        if(channelTypeUuidList.contains("all")) {
                            channelTypeUuidList.clear();
                        }
                        for(ChannelRelationVo channelRelation : channelRelationTargetList) {
                            if("channel".equals(channelRelation.getType())) {
                                return true;
                            }else if("catalog".equals(channelRelation.getType())) {
                                if(channelMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(channelRelation.getTarget(), channelTypeUuidList) > 0) {
                                    return true;
                                }else {
                                    CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelRelation.getTarget());
                                    if(catalogVo != null) {
                                        List<String> uuidList = catalogMapper.getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                                        for(String uuid : uuidList) {
                                            if(!channelRelation.getTarget().equals(uuid)) {
                                                if(channelMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(channelRelation.getTarget(), channelTypeUuidList) > 0) {
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
        
        operationBiPredicateMap.put(ProcessTaskOperationType.COPYPROCESSTASK, (processTaskVo, processTaskStepVo) -> {
            if(catalogService.channelIsAuthority(processTaskVo.getChannelUuid())) {
                return true;
            }else {
                return false;
            }
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.REDO, (processTaskVo, processTaskStepVo) -> {
            if(ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
                ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskVo.getId());
                if(processTaskScoreTemplateVo != null) {
                   if(StringUtils.isNotBlank(processTaskScoreTemplateVo.getConfigHash())) {
                       String configStr = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(processTaskScoreTemplateVo.getConfigHash());
                       if(StringUtils.isNotBlank(configStr)) {
                           processTaskScoreTemplateVo.setConfig(configStr);
                           if(CollectionUtils.isNotEmpty(processTaskScoreTemplateVo.getConfig().getJSONArray("stepUuidList"))) {
                               return true;
                           }
                       }
                   }
                }
            }
            return false;
        });
        
        operationBiPredicateMap.put(ProcessTaskOperationType.TRANSFER, (processTaskVo, processTaskStepVo) -> {
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                if(CollectionUtils.isEmpty(processTaskVo.getStepList())) {
                    processTaskVo.getStepList().addAll(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId()));
                }
                for (ProcessTaskStepVo processTaskStep : processTaskVo.getStepList()) {
                    if (processTaskStep.getIsActive().intValue() == 1) {
                        if(processTaskService.checkOperationAuthIsConfigured(processTaskStep, processTaskVo.getOwner(), processTaskVo.getReporter(), ProcessTaskOperationType.TRANSFERCURRENTSTEP)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    @Override
    public OperationAuthHandlerType getHandler() {
        return OperationAuthHandlerType.TASK;
    }
    @Override
    public Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }
}
