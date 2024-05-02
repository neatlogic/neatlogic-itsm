/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.dao.mapper.processtask;

import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.process.crossover.IProcessTaskCrossoverMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.automatic.ProcessTaskStepAutomaticRequestVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessTaskMapper extends IProcessTaskCrossoverMapper {

    List<ProcessTaskVo> getProcessTaskByStatusList(@Param("statusList") List<String> statusList, @Param("count") Integer count);

    /**
     * 获取工单基本信息（已删除则忽略）
     *
     * @param processTaskId
     * @return
     */
    ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

    /**
     * 获取工单基本信息（不限是否已删除）
     *
     * @param processTaskId
     * @return
     */
    ProcessTaskVo getProcessTaskBaseInfoByIdIncludeIsDeleted(Long processTaskId);

    List<ProcessTaskVo> getTaskListByIdList(List<Long> idList);

    /**
     * 查询待处理的工单id
     *
     * @param map 工单查询条件
     * @return 工单ID列表
     */
    List<Long> getProcessingTaskIdListByCondition(@Param("conditionMap") Map<String, Object> map);

    /**
     * 查询待处理的工单数量
     *
     * @param map 工单查询条件
     * @return 工单数量
     */
    int getProcessingTaskCountByCondition(@Param("conditionMap") Map<String, Object> map);

    List<ProcessTaskStepVo> getProcessTaskStepBaseInfoByProcessTaskId(Long processTaskId);

//    List<Long> getProcessTaskStepIdByConvergeId(Long convergeId);

    ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

    List<ProcessTaskFormAttributeVo> getProcessTaskFormExtendAttributeListByProcessTaskIdAndTag(@Param("processTaskId") Long processTaskId, @Param("tag") String tag);

    List<ProcessTaskFormVo> getProcessTaskFormListByProcessTaskIdList(List<Long> existsProcessTaskIdList);

    List<Long> getProcessTaskFormAttributeDataIdListByProcessTaskId(Long processTaskId);

    List<AttributeDataVo> getProcessTaskFormAttributeDataListByProcessTaskId(Long processTaskId);

    List<Long> getProcessTaskExtendFormAttributeDataIdListByProcessTaskId(Long processTaskId);

    List<AttributeDataVo> getProcessTaskExtendFormAttributeDataListByProcessTaskId(@Param("processTaskId") Long processTaskId, @Param("tag") String tag);

    List<ProcessTaskStepContentVo> getProcessTaskStepContentByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepContentVo> getProcessTaskStepContentByProcessTaskId(Long processTaskId);

    String getProcessTaskStartContentByProcessTaskId(Long processTaskId);

    List<ProcessTaskStepUserVo> getProcessTaskStepUserByStepId(
            @Param("processTaskStepId") Long processTaskStepId, @Param("userType") String userType);

    List<ProcessTaskStepUserVo> getProcessTaskStepUserByStepIdList(
            @Param("processTaskStepIdList") List<Long> processTaskStepIdList, @Param("userType") String userType);

    List<ProcessTaskStepWorkerPolicyVo>
    getProcessTaskStepWorkerPolicy(ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo);

    List<ProcessTaskStepWorkerVo> getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
            @Param("processTaskId") Long processTaskId, @Param("processTaskStepId") Long processTaskStepId);

    List<ProcessTaskStepWorkerVo> getProcessTaskStepWorkerListByProcessTaskIdList(List<Long> processTaskIdList);

    List<ProcessTaskStepWorkerVo> getProcessTaskStepWorkerListByProcessTaskStepIdListAndUserType(@Param("processTaskStepIdList") List<Long> processTaskStepIdList, @Param("userType") String userType);

    int checkProcessTaskStepWorkerIsExistsByPrimaryKey(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

    Long getProcessTaskLockById(Long processTaskId);

    ProcessTaskVo getProcessTaskVoLockById(Long processTaskId);

    int checkProcessTaskConvergeIsExists(ProcessTaskConvergeVo processTaskStepConvergeVo);

    List<ProcessTaskConvergeVo> getProcessTaskConvergeListByStepId(Long processTaskStepId);

//    List<ProcessTaskStepVo> getFromProcessTaskStepByToId(Long toProcessTaskStepId);

    List<Long> getFromProcessTaskStepIdListByToId(Long toProcessTaskStepId);

    List<ProcessTaskStepVo> getToProcessTaskStepByFromIdAndType(
            @Param("fromProcessTaskStepId") Long fromProcessTaskStepId, @Param("type") String type);

    List<Long> getToProcessTaskStepIdListByFromIdAndType(
            @Param("fromProcessTaskStepId") Long fromProcessTaskStepId, @Param("type") String type);

    List<ProcessTaskStepVo> getProcessTaskStepByConvergeId(Long convergeId);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelByFromId(Long fromProcessTaskStepId);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelListByFromIdList(List<Long> fromProcessTaskStepIdList);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelByToId(Long toProcessTaskStepId);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelListByToIdList(List<Long> toStepIdList);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelByProcessTaskId(Long processTaskId);

    List<ProcessTaskStepRelVo> getProcessTaskStepRelListByProcessTaskIdList(List<Long> processTaskIdList);

    ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId);

    ProcessTaskStepVo getEndProcessTaskStepByProcessTaskId(Long processTaskId);

    List<ProcessTaskStepVo> getProcessTaskStepByProcessTaskIdAndType(@Param("processTaskId") Long processTaskId,
                                                                     @Param("type") String type);

    List<ProcessTaskStepVo> getProcessTaskActiveStepByProcessTaskIdAndProcessStepType(
            @Param("processTaskId") Long processTaskId, @Param("processStepTypeList") List<String> processStepTypeList,
            @Param("isActive") Integer isActive);

    ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

    ProcessTaskVo getProcessTaskById(Long id);

    List<ProcessTaskVo> getProcessTaskByIdStrList(List<String> idList);

    List<ProcessTaskVo> getProcessTaskBySerialNumberList(List<String> serialNumberList);

    List<ProcessTaskStepAuditVo> getProcessTaskStepAuditList(ProcessTaskStepAuditVo processTaskStepAuditVo);

    List<ProcessTaskStepVo> getProcessTaskStepListByProcessTaskId(Long processTaskId);

    List<ProcessTaskStepVo> getProcessTaskStepListByProcessTaskIdList(List<Long> processTaskIdList);

    Set<Long> getProcessTaskStepIdSetByChannelUuidListAndAuthenticationInfo(
            @Param("keyword") String keyword,
            @Param("channelUuidList") List<String> channelUuidList,
            @Param("authenticationInfoVo") AuthenticationInfoVo authenticationInfoVo
    );

    Set<Long> getProcessTaskIdSetByChannelUuidListAndAuthenticationInfo(@Param("channelUuidList") List<String> channelUuidList, @Param("authenticationInfoVo") AuthenticationInfoVo authenticationInfoVo);

    int checkIsWorker(@Param("processTaskId") Long processTaskId,
                      @Param("processTaskStepId") Long processTaskStepId, @Param("userType") String userType,
                      @Param("authenticationInfoVo") AuthenticationInfoVo authenticationInfoVo);

    int checkIsProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

    List<ProcessTaskAssignWorkerVo> getProcessTaskAssignWorker(ProcessTaskAssignWorkerVo processTaskAssignWorkerVo);

    ProcessTaskStepVo getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuid(
            @Param("processTaskId") Long processTaskId, @Param("processStepUuid") String processStepUuid);

    List<ProcessTaskStepAuditVo> getProcessTaskAuditList(ProcessTaskStepAuditVo processTaskStepAuditVo);

    List<ProcessTaskVo> getProcessTaskListByIdList(List<Long> processTaskIdList);

    List<ProcessTaskStepVo> getProcessTaskStepListByIdList(List<Long> processTaskStepIdList);

//    ProcessTaskStepNotifyPolicyVo getProcessTaskStepNotifyPolicy(ProcessTaskStepNotifyPolicyVo processTaskStepNotifyPolicyVo);

    Map<String, String> getProcessTaskOldFormAndPropByTaskId(Long processTaskId);

    List<Map<String, Object>> getWorkloadByTeamUuid(String teamUuid);

    List<Long> getFileIdListByContentId(Long contentId);

    ProcessTaskStepContentVo getProcessTaskStepContentById(Long id);

    List<ProcessTaskStepUserVo> getProcessTaskStepUserList(ProcessTaskStepUserVo processTaskStepUserVo);

    List<ProcessTaskStepUserVo> getProcessTaskStepUserListByProcessTaskIdList(List<Long> processTaskIdList);

    List<ProcessTaskStepUserVo> getProcessTaskStepUserListByProcessTaskIdListAndStatusList(@Param("processTaskIdList") List<Long> processTaskIdList, @Param("statusList") List<String> statusList);

    String getProcessTaskScoreInfoById(Long processtaskId);

    ProcessTaskVo getProcessTaskAndStepById(Long processtaskId);

    Long getFromProcessTaskIdByToProcessTaskId(Long toProcessTaskId);

    List<Long> getToProcessTaskIdListByFromProcessTaskId(Long processTaskId);

    int getProcessTaskRelationCountByProcessTaskId(Long processTaskId);

    List<ProcessTaskRelationVo> getProcessTaskRelationList(ProcessTaskRelationVo processTaskRelationVo);

    List<Long> getRelatedProcessTaskIdListByProcessTaskId(Long processTaskId);

    List<Long> checkProcessTaskIdListIsExists(List<Long> processTaskIdList);

    int getProcessTaskCountByKeywordAndChannelUuidList(ProcessTaskSearchVo processTaskSearchVo);

    List<ProcessTaskVo> getProcessTaskListByKeywordAndChannelUuidList(ProcessTaskSearchVo processTaskSearchVo);

    ProcessTaskTransferReportVo getProcessTaskTransferReportByToProcessTaskId(Long toProcessTaskId);

    ProcessTaskRelationVo getProcessTaskRelationById(Long id);

    List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId);

    int searchProcessTaskImportAuditCount(ProcessTaskImportAuditVo processTaskImportAuditVo);

    List<ProcessTaskImportAuditVo> searchProcessTaskImportAudit(ProcessTaskImportAuditVo processTaskImportAuditVo);

    ProcessTaskScoreTemplateVo getProcessTaskScoreTemplateByProcessTaskId(Long processTaskId);

    ProcessTaskStepAgentVo getProcessTaskStepAgentByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepAgentVo> getProcessTaskStepAgentListByProcessTaskIdList(List<Long> processTaskIdList);

    int checkProcessTaskFocusExists(@Param("processTaskId") Long processTaskId,
                                    @Param("userUuid") String userUuid);

//    List<String> getFocusUsersOfProcessTask(Long processTaskId);

    List<String> getFocusUserListByTaskId(Long processTaskId);

    List<ProcessTagVo> getProcessTaskTagListByProcessTaskId(@Param("processTaskId") Long processTaskId);

    int getProcessTaskStepInOperationCountByProcessTaskId(Long processTaskId);

    List<ProcessTaskStepInOperationVo> getProcessTaskStepInOperationListByProcessTaskId(Long processTaskId);

    int getProcessTaskCountByChannelTypeUuidAndStartTime(ProcessTaskVo processTaskVo);

    List<ProcessTaskVo> getProcessTaskListByChannelTypeUuidAndStartTime(ProcessTaskVo processTaskVo);

    String getProcessTaskStepNameById(Long id);

    Integer getProcessTaskCountBySql(String searchSql);

    List<Map<String, Object>> getWorkcenterProcessTaskMapBySql(String searchSql);

    List<ProcessTaskVo> getProcessTaskBySql(String searchSql);

    List<ProcessTaskStepVo> getProcessTaskCurrentStepByProcessTaskId(Long processTaskId);

    Long getProcessTaskIdByChannelUuidLimitOne(String channelUuid);

    Long getProcessTaskIdByPriorityUuidLimitOne(String prioriryUuid);

    List<ChannelVo> getChannelReferencedCountList();

    List<ProcessTaskStepFileVo> getProcessTaskStepFileListByTaskId(Long taskId);

    List<ProcessTaskStepFileVo> getProcessTaskStepFileListByTaskStepId(Long taskId);

    Long getRepeatGroupIdByProcessTaskId(Long processTaskId);

    List<Long> getProcessTaskIdListByRepeatGroupId(Long repeatGroupId);

    Integer getProcessTaskStepReapprovalRestoreBackupMaxSortByBackupStepId(Long processTaskStepId);

    List<ProcessTaskStepReapprovalRestoreBackupVo> getProcessTaskStepReapprovalRestoreBackupListByBackupStepId(Long processTaskStepId);

    List<ProcessTaskVo> getProcessTaskColumnByIndexKeyword(@Param("keywordList") List<String> keywordList, @Param("limit") int limit, @Param("targetType") String targetType, @Param("columnPro") String columnPro);

    List<ProcessTaskVo> getProcessTaskIdAndTitleByIndexKeyword(@Param("keywordList") List<String> keywordList, @Param("limit") int limit);

    List<Long> getProcessTaskStepIdListByProcessTaskIdAndTagId(ProcessTaskStepTagVo processTaskStepTagVo);

    int checkProcessTaskStepTagIsExists(ProcessTaskStepTagVo processTaskStepTagVo);

    List<Long> getSameTagIdListByProcessTaskStepIdList(List<Long> processTaskStepIdList);

    List<Long> getTagIdListByProcessTaskStepId(Long processTaskStepId);

    int getProcessTaskCountByOwner(ProcessTaskVo vo);

    List<ProcessTaskVo> getProcessTaskListByOwner(ProcessTaskVo vo);

    ProcessTaskStepAutomaticRequestVo getProcessTaskStepAutomaticRequestById(Long id);

    List<ProcessTaskStepAutomaticRequestVo> getAllProcessTaskStepAutomaticRequestList();

    List<ProcessTaskVo> getProcessTaskStepVoListByFileId(Long fileId);

    ProcessTaskStepTimerVo getProcessTaskStepTimerByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepTimerVo> getAllProcessTaskStepTimerList();

    List<ProcessTaskStepVo> getProcessTaskStepListByProcessTaskIdAndProcessStepUuidList(@Param("processTaskId") Long processTaskId, @Param("processStepUuidList") List<String> processStepUuidList);

    Integer getAllProcessTaskCount();

    List<Long> getProcessTaskIdList(ProcessTaskVo processTaskVo);

    List<ProcessTaskStepVo> getProcessTaskStepByProcessTaskIdAndStepName(ProcessTaskStepVo vo);

    List<Map<String, Object>> getProcessTaskListWhichIsProcessingByUserAndTag(@Param("tag") String tag, @Param("userUuid") String userUuid, @Param("teamUuidList") List<String> teamUuidList, @Param("roleUuidList") List<String> roleUuidList);

    /**
     * 根据给定的工单ID，查询其中当前步骤超过一个的工单id列表
     *
     * @param processTaskIdList 工单ID列表
     * @return
     */
    List<Long> getProcessTaskIdListWhichCurrentProcessTaskStepCountIsOverOneByProcessTaskIdList(@Param("list") List<Long> processTaskIdList);

    List<ProcessTaskStepVo> getCurrentProcessTaskStepListByProcessTaskIdListAndTag(@Param("list") List<Long> processTaskIdList, @Param("tag") String tag);

    List<ProcessTaskFormVo> getProcessTaskFormContentListByContentLikeKeyword(String formstaticlist);

    List<ProcessTaskFormVo> getProcessTaskFormContentList();

    List<FileVo> getFileListByProcessTaskId(Long processTaskId);

    List<FileVo> getFileDetailListByProcessTaskId(Long processTaskId);

    /**
     * 根据工单ID查询所有子任务附件
     *
     * @param processTaskId 工单ID
     * @return
     */
    List<FileVo> getProcessTaskStepTaskFileListByProcessTaskId(Long processTaskId);

    String getProcessTaskLastUrgeUserUuidByProcessTaskId(Long processTaskId);

    int getProcessTaskUrgeCountByProcessTaskId(Long processTaskId);

    int searchProcessTaskCountByOwnerAndExcludeId(ProcessTaskSearchVo searchVo);

    List<ProcessTaskVo> searchProcessTaskListByOwnerAndExcludeId(ProcessTaskSearchVo searchVo);

    int getSubProcessTaskCountByStepId(Long processTaskStepId);

    List<ProcessTaskVo> SearchSubProcessTaskListByStepId(Long processTaskStepId);

    ProcessTaskInvokeVo getInvokeByProcessTaskId(Long processTaskId);

    int getSubProcessTaskCountByStepIdAndWithoutEndStatusList(@Param("processTaskStepId") Long processTaskStepId,@Param("statusList") List<String> statusList);

    int insertIgnoreProcessTaskConfig(ProcessTaskConfigVo processTaskConfigVo);

    int replaceProcessTaskOldFormProp(@Param("processTaskId") Long processTaskId, @Param("form") String form,
                                      @Param("prop") String prop);

    int insertProcessTaskForm(ProcessTaskFormVo processTaskFormVo);

    int insertIgnoreProcessTaskFormContent(ProcessTaskFormVo processTaskFormVo);

    int insertProcessTask(ProcessTaskVo processTaskVo);

    int replaceProcessTask(ProcessTaskVo processTaskVo);

    int insertIgnoreProcessTaskContent(ProcessTaskContentVo processTaskContentVo);

    int batchInsertIgnoreProcessTaskContent(List<ProcessTaskContentVo> list);

    int insertProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

    int replaceProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

    int insertProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

    int insertProcessTaskStepMinorUser(ProcessTaskStepUserVo processTaskStepUserVo);

    int insertProcessTaskStepWorkerPolicy(ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo);

    int insertProcessTaskStepRel(ProcessTaskStepRelVo processTaskStepRelVo);

    int insertProcessTaskStepContent(ProcessTaskStepContentVo processTaskStepContentVo);

    int insertProcessTaskOperationContent(ProcessTaskOperationContentVo processTaskOperationContentVo);

    int insertProcessTaskStepAudit(ProcessTaskStepAuditVo processTaskStepAuditVo);

    int batchInsertProcessTaskStepAudit(List<ProcessTaskStepAuditVo> list);

    int insertProcessTaskStepAuditDetail(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo);

    int batchInsertProcessTaskStepAuditDetail(List<ProcessTaskStepAuditDetailVo> list);

    int insertIgnoreProcessTaskStepWorker(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

    int insertIgnoreProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

    int insertIgnoreProcessTaskConverge(ProcessTaskConvergeVo processTaskConvergeVo);

    int insertIgnoreProcessTaskStepConfig(ProcessTaskStepConfigVo processTaskStepConfigVo);

//    int insertProcessTaskStepFormAttribute(
//            ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

    int insertProcessTaskFormAttribute(ProcessTaskFormAttributeDataVo processTaskFromAttributeDataVo);

    int insertProcessTaskExtendFormAttribute(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo);

    int insertProcessTaskStepFile(ProcessTaskStepFileVo processTaskStepFileVo);

    int insertProcessTaskAssignWorker(ProcessTaskAssignWorkerVo processTaskAssignWorkerVo);

//    int insertIgnoreProcessTaskStepNotifyPolicyConfig(ProcessTaskStepNotifyPolicyVo processTaskStepNotifyPolicyVo);

    int insertProcessTaskStepNotifyPolicy(ProcessTaskStepNotifyPolicyVo processTaskStepNotifyPolicyVo);

    int insertProcessTaskTransferReport(ProcessTaskTransferReportVo processTaskTransferReportVo);

    int replaceProcessTaskRelation(ProcessTaskRelationVo processTaskRelationVo);

    int insertProcessTaskStepRemind(ProcessTaskStepRemindVo processTaskStepRemindVo);

    int batchInsertProcessTaskImportAudit(@Param("list") List<ProcessTaskImportAuditVo> processTaskImportAuditVos);

    int insertProcessTaskScoreTemplate(ProcessTaskScoreTemplateVo processTaskScoreTemplateVo);

    int insertProcessTaskScoreTemplateConfig(ProcessTaskScoreTemplateConfigVo processTaskScoreTemplateConfigVo);

    int insertProcessTaskFocus(@Param("processTaskId") Long processTaskId,
                               @Param("userUuid") String userUuid);

    int insertProcessTaskTag(@Param("processTaskTagList") List<ProcessTaskTagVo> processTaskTagList);

    int replaceProcessTaskStepAgent(ProcessTaskStepAgentVo processTaskStepAgentVo);

    int insertProcessTaskStepInOperation(ProcessTaskStepInOperationVo processTaskStepInOperationVo);

    int insertProcessTaskStepTag(ProcessTaskStepTagVo processTaskStepTagVo);

    int replaceProcessTaskRepeatList(List<ProcessTaskRepeatVo> processTaskRepeatList);

    int replaceProcessTaskRepeat(ProcessTaskRepeatVo processTaskRepeatVo);

    int insertProcessTaskStepReapprovalRestoreBackup(ProcessTaskStepReapprovalRestoreBackupVo processTaskStepReapprovalRestoreBackupVo);

    int insertProcessTaskStepAutomaticRequest(ProcessTaskStepAutomaticRequestVo processTaskStepAutomaticRequestVo);

    int insertProcessTaskStepTimer(ProcessTaskStepTimerVo processTaskStepTimerVo);

    void insertProcessTaskTimeCost(ProcessTaskTimeCostVo processTaskTimeCostVo);

    void insertProcessTaskUrge(@Param("processTaskId") Long processTaskId, @Param("lcu") String lcu);

    int insertProcessTaskInvoke(@Param("processTaskId") Long processTaskId, @Param("source") String invoke, @Param("sourceType") String invokeType, @Param("invokeId") Long invokeId);

    int insertProcessTaskFormExtendAttribute(ProcessTaskFormAttributeVo processTaskFormAttributeVo);

    int updateProcessTaskStepStatus(ProcessTaskStepVo processTaskStepVo);

    int updateProcessTaskStatus(ProcessTaskVo processTaskVo);

    //    int updateProcessTaskStepRelIsHit(@Param("fromProcessTaskStepId") Long fromProcessTaskStepId, @Param("toProcessTaskStepId") Long toProcessTaskStepId, @Param("isHit") Integer isHit);
    int updateProcessTaskStepRelIsHit(ProcessTaskStepRelVo processTaskStepRelVo);

//    int updateProcessTaskStepConvergeIsCheck(@Param("isCheck") Integer isCheck,
//                                                    @Param("convergeId") Long convergeId, @Param("processTaskStepId") Long processTaskStepId);

    int updateProcessTaskStepUserStatus(ProcessTaskStepUserVo processTaskStepUserVo);

    int updateProcessTaskIsShow(ProcessTaskVo processTaskVo);

    int updateProcessTaskNeedScoreByIdList(@Param("idList") List<Long> idList, @Param("needScore") Integer needScore);

    int updateProcessTaskTitleOwnerPriorityUuid(ProcessTaskVo processTaskVo);

    int updateProcessTaskStepContentById(ProcessTaskStepContentVo processTaskStepContentVo);

    int updateProcessTaskStepWorkerUuid(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

    int updateProcessTaskStepUserUserUuid(ProcessTaskStepUserVo processTaskStepUserVo);

    int updateProcessTaskPriorityUuidById(@Param("id") Long processTaskId,
                                          @Param("priorityUuid") String priorityUuid);

    int updateProcessTaskSerialNumberById(@Param("id") Long processTaskId,
                                          @Param("serialNumber") String serialNumber);

    int updateProcessTaskStepAutomaticRequestTriggerTimeById(ProcessTaskStepAutomaticRequestVo processTaskStepAutomaticRequestVo);

    int updateProcessTaskStepTimerTriggerTimeById(ProcessTaskStepTimerVo processTaskStepTimerVo);

    int updateProcessTaskStepStatusByStepId(ProcessTaskStepVo processTaskStepVo);

    int updateProcessTaskStepMajorUserAndStatus(ProcessTaskStepUserVo processTaskStepUserVo);

    int updateProcessTaskFormFormContentHashByFormContentHash(@Param("oldFormConfigHash") String oldFormConfigHash, @Param("newFormConfigHash") String newFormConfigHash);

    int updateProcessTaskIsDeletedById(@Param("id") Long id, @Param("isDeleted") Integer isDeleted);

    int deleteProcessTaskFormAttributeByProcessTaskId(Long processTaskId);

    int deleteProcessTaskExtendFormAttributeByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepWorker(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

    int deleteProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

    int deleteProcessTaskConvergeByStepId(Long processTaskStepId);

    int deleteProcessTaskAssignWorker(ProcessTaskAssignWorkerVo processTaskAssignWorkerVo);

    int deleteProcessTaskStepFileByProcessTaskStepId(@Param("processTaskId") Long processTaskId, @Param("processTaskStepId") Long processTaskStepId);

    int deleteProcessTaskStepContentByProcessTaskStepId(Long processTaskStepId);

    int deleteProcessTaskStepFileByContentId(Long contentId);

    int deleteProcessTaskStepContentById(Long contentId);

    int deleteProcessTaskRelationById(Long processTaskRelationId);

    int deleteProcessTaskStepRemind(ProcessTaskStepRemindVo processTaskStepRemindVo);

    int deleteProcessTaskFocus(@Param("processTaskId") Long processTaskId,
                               @Param("userUuid") String userUuid);

    int deleteProcessTaskStepAgentByProcessTaskStepId(Long processTaskStepId);

    int deleteProcessTaskAssignWorkerByProcessTaskId(Long processTaskId);

    int deleteProcessTaskConvergeByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepFileByProcessTaskId(Long processTaskId);

    int deleteProcessTaskFormByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepByProcessTaskId(Long processTaskId);

    int deleteProcessTaskByProcessTaskId(Long processTaskId);

    int deleteProcessTaskFocusByProcessTaskId(Long processTaskId);

    int deleteProcessTaskTagByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepInOperationById(Long id);

    int deleteProcessTaskStepInOperationByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepWorkerMinorByProcessTaskStepId(Long processTaskStepId);

    int deleteProcessTaskStepUserMinorByProcessTaskStepId(Long processTaskStepId);

    int deleteProcessTaskRepeatByProcessTaskId(Long processTaskId);

    int deleteProcessTaskStepReapprovalRestoreBackupByBackupStepId(Long processTaskStepId);

    int deleteProcessTaskStepAutomaticRequestById(Long id);

    int deleteProcessTaskStepAutomaticRequestByProcessTaskStepId(Long id);

    int deleteProcessTaskStepTimerByProcessTaskStepId(Long processTaskStepId);

    int deleteProcessTaskTimeCostByProcessTaskId(Long processTaskId);

    int deleteProcessTaskFormContentByHash(String hash);
}
