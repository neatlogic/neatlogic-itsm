package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.ProcessTaskAssignUserVo;
import codedriver.module.process.dto.ProcessTaskConfigVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskConvergeVo;
import codedriver.module.process.dto.ProcessTaskFileVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskSlaNotifyVo;
import codedriver.module.process.dto.ProcessTaskSlaTimeVo;
import codedriver.module.process.dto.ProcessTaskSlaTransferVo;
import codedriver.module.process.dto.ProcessTaskSlaVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepConfigVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepTeamVo;
import codedriver.module.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

public interface ProcessTaskMapper {
	public ProcessTaskSlaVo getProcessTaskSlaById(Long slaId);

	public List<ProcessTaskSlaNotifyVo> getAllProcessTaskSlaNotify();

	public List<ProcessTaskSlaTransferVo> getAllProcessTaskSlaTransfer();

	public List<ProcessTaskStepVo> getProcessTaskStepBaseInfoBySlaId(Long slaId);

	public ProcessTaskSlaTimeVo getProcessTaskSlaTimeBySlaId(Long slaId);

	public ProcessTaskSlaNotifyVo getProcessTaskNotifyById(Long id);

	public ProcessTaskSlaTransferVo getProcessTaskSlaTransferById(Long id);

	public ProcessTaskConfigVo getProcessTaskConfigByHash(String hash);

	public List<ProcessTaskSlaVo> getProcessTaskSlaByProcessTaskStepId(Long processTaskStepId);

	public ProcessTaskStepAuditDetailVo getProcessTaskStepAuditDetail(@Param("processTaskId") Long processTaskId, @Param("type") String type);

	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

	public List<ProcessTaskStepVo> getProcessTaskStepBaseInfoByProcessTaskId(Long processTaskId);

	public List<Long> getProcessTaskStepIdByConvergeId(Long convergeId);

	public String getProcessTaskStepConfigByHash(String hash);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskFormAttributeDataVo> getProcessTaskStepFormAttributeDataByProcessTaskId(Long processTaskId);

	public List<ProcessTaskFormAttributeDataVo> getProcessTaskStepFormAttributeDataByProcessTaskStepId(Long processTaskStepId);

	public List<ProcessTaskStepContentVo> getProcessTaskStepContentProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepContentVo> getProcessTaskStepContentProcessTaskStepId(Long processTaskStepId);

	public ProcessTaskContentVo getProcessTaskContentByHash(String hash);

	public List<ProcessTaskStepUserVo> getProcessTaskStepUserByStepId(@Param("processTaskStepId") Long processTaskStepId, @Param("userType") String userType);

	public List<ProcessTaskStepVo> searchProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

	public List<ProcessTaskAssignUserVo> getProcessAssignUserByToStepId(Long toStepId);

	public int checkProcessTaskStepUserIsExists(ProcessTaskStepUserVo processTaskStepUserVo);

	public List<ProcessTaskStepTimeoutPolicyVo> getProcessTaskStepTimeoutPolicyByProcessTaskStepId(Long processTaskStepId);

	public List<ProcessTaskStepWorkerPolicyVo> getProcessTaskStepWorkerPolicyByProcessTaskStepId(Long processTaskStepId);

	public List<ProcessTaskStepWorkerVo> getProcessTaskStepWorkerByProcessTaskStepId(Long processTaskStepId);

	public Long getProcessTaskLockById(Long processTaskId);

	public int checkProcessTaskConvergeIsExists(ProcessTaskConvergeVo processTaskStepConvergeVo);

	public List<ProcessTaskStepVo> getFromProcessTaskStepByToId(Long toProcessTaskStepId);

	public List<ProcessTaskStepVo> getToProcessTaskStepByFromId(Long fromProcessTaskStepId);

	public List<ProcessTaskStepVo> getProcessTaskStepByConvergeId(Long convergeId);

	public List<ProcessTaskStepRelVo> getProcessTaskStepRelByFromId(Long fromProcessTaskStepId);

	public List<ProcessTaskStepRelVo> getProcessTaskStepRelByToId(Long toProcessTaskStepId);

	public List<ProcessTaskStepRelVo> getProcessTaskStepRelByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepVo> getProcessTaskStepByProcessTaskIdAndType(@Param("processTaskId") Long processTaskId, @Param("type") String type);
	
	public List<ProcessTaskStepVo> getProcessTaskActiveStepByProcessTaskId(@Param("processTaskId") Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public ProcessTaskVo getProcessTaskById(Long id);
	
	public List<ProcessTaskFileVo> searchProcessTaskFile(ProcessTaskFileVo processTaskFileVo);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByProcessTaskStepId(Long processTaskStepId);

	public int replaceProcessTaskConfig(ProcessTaskConfigVo processTaskConfigVo);

	public int insertProcessTaskForm(ProcessTaskFormVo processTaskFormVo);

	public int replaceProcessTaskFormContent(ProcessTaskFormVo processTaskFormVo);

	public int insertProcessTask(ProcessTaskVo processTaskVo);

	public int insertProcessTaskChannel(ProcessTaskVo processTaskVo);

	public int replaceProcessTaskContent(ProcessTaskContentVo processTaskContentVo);

	public int insertProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

	public int insertProcessTaskSlaNotify(ProcessTaskSlaNotifyVo processTaskSlaNotifyVo);

	public int insertProcessTaskSlaTransfer(ProcessTaskSlaTransferVo processTaskSlaTransferVo);

	public int insertProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

	public int insertProcessTaskStepWorkerPolicy(ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo);

	public int insertProcessTaskStepTeam(ProcessTaskStepTeamVo processTaskStepTeamVo);

	public int insertProcessTaskStepRel(ProcessTaskStepRelVo processTaskStepRelVo);

	public int insertProcessTaskStepContent(ProcessTaskStepContentVo processTaskStepContentVo);

	public int insertProcessTaskStepAudit(ProcessTaskStepAuditVo processTaskStepAuditVo);

	public int insertProcessTaskStepAuditDetail(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo);

	public int insertProcessTaskStepAuditFormAttributeData(ProcessTaskStepAuditFormAttributeDataVo processTaskStepAuditFormAttributeDataVo);

	public int insertProcessTaskStepWorker(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

	public int insertProcessTaskConverge(ProcessTaskConvergeVo processTaskConvergeVo);

	public int insertProcessTaskStepTimeoutPolicy(ProcessTaskStepTimeoutPolicyVo processTaskStepTimeoutPolicy);

	public int replaceProcessTaskStepConfig(ProcessTaskStepConfigVo processTaskStepConfigVo);

	public int insertProcessTaskStepFormAttribute(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	public int insertProcessTaskSla(ProcessTaskSlaVo processTaskSlaVo);

	public int insertProcessTaskSlaTime(ProcessTaskSlaTimeVo processTaskSlaTimeVo);

	public int insertProcessTaskStepSla(@Param("processTaskStepId") Long processTaskStepId, @Param("slaId") Long slaId);

	public int replaceProcessTaskFormAttributeData(ProcessTaskFormAttributeDataVo processTaskFromAttributeDataVo);
	
	public int insertProcessTaskFile(ProcessTaskFileVo processTaskFileVo);

	public int updateProcessTaskStepExpireTime(ProcessTaskStepVo processTaskStepVo);

	public int updateProcessTaskStepStatus(ProcessTaskStepVo processTaskStepVo);

	public int updateProcessTaskStatus(ProcessTaskVo processTaskVo);

	public int updateProcessTaskSlaNotify(ProcessTaskSlaNotifyVo processTaskNotifyVo);

	public int updateProcessTaskSlaTime(ProcessTaskSlaTimeVo processTaskSlaTimeVo);

	public int updateProcessTaskSlaTransfer(ProcessTaskSlaTransferVo processTaskSlaTransferVo);

	public int updateProcessTaskStepRelIsHit(@Param("fromProcessTaskStepId") Long fromProcessTaskStepId, @Param("toProcessTaskStepId") Long toProcessTaskStepId, @Param("isHit") Integer isHit);

	public int updateProcessTaskStepConvergeIsCheck(@Param("isCheck") Integer isCheck, @Param("convergeId") Long convergeId, @Param("processTaskStepId") Long processTaskStepId);

	public int updateProcessTaskStepUserStatus(ProcessTaskStepUserVo processTaskStepUserVo);
	
	public int updateProcessTaskTitleOwnerPriorityUuid(ProcessTaskVo processTaskVo);

	public int deleteProcessTaskFormAttributeValueByProcessTaskIdAndAttributeUuid(@Param("processTaskId") Long processTaskId, @Param("attributeUuid") String attributeUuid);

	public int deleteProcessTaskStepWorker(@Param("processTaskStepId") Long processTaskStepId, @Param("userId") String userId);

	public int deleteProcessTaskStepUser(@Param("processTaskStepId") Long processTaskStepId, @Param("userType") String userType);

	public int deleteProcessTaskConvergeByStepId(Long processTaskStepId);

	public int deleteProcessTaskSlaNotifyById(Long slaNotifyId);

	public int deleteProcessTaskStepWorkerByProcessTaskId(Long processTaskId);

	public int deleteProcessTaskSlaTransferById(Long slaTransferId);
	
	public int deleteProcessTaskFile(ProcessTaskFileVo processTaskFileVo);

}
