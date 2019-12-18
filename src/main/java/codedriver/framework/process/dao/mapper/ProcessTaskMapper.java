package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.ProcessTaskAssignUserVo;
import codedriver.module.process.dto.ProcessTaskAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskAttributeValueVo;
import codedriver.module.process.dto.ProcessTaskAttributeVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskConvergeVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepAuditAttributeDataVo;
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
	public List<ProcessTaskAttributeValueVo> getProcessTaskAttributeValueByProcessTaskId(Long processTaskId);

	public List<ProcessTaskAttributeDataVo> getProcessTaskAttributeDataByProcessTaskId(Long processTaskId);

	public String getProcessTaskStepConfigByStepId(Long processTaskStepId);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskAttributeDataVo> getProcessTaskStepFormAttributeDataByStepId(Long processTaskStepId);

	public List<ProcessTaskAttributeDataVo> getProcessTaskStepAttributeDataByStepId(Long processTaskStepId);

	public Long getProcessTaskStepContentIdByProcessTaskStepId(Long processTaskStepId);

	public List<ProcessTaskStepUserVo> getProcessTaskStepUserByStepId(Long processTaskStepId);

	public List<ProcessTaskStepVo> searchProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

	public List<ProcessTaskAssignUserVo> getProcessAssignUserByToStepId(Long toStepId);

	public int checkProcessTaskStepUserIsExists(ProcessTaskStepUserVo processTaskStepUserVo);

	public List<ProcessTaskAttributeValueVo> getProcessTaskAttributeValue(@Param("processTaskId") Long processTaskId, @Param("attributeUuid") String attributeUuid);

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

	public List<ProcessTaskStepVo> getProcessTaskStepByProcessTaskIdAndType(@Param("processTaskId") Long processTaskId, @Param("type") String type);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	public List<ProcessTaskStepAttributeVo> getProcessTaskStepAttributeByStepId(ProcessTaskStepAttributeVo processTaskStepAttributeVo);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public int insertProcessTaskForm(ProcessTaskFormVo processTaskFormVo);

	public int insertProcessTask(ProcessTaskVo processTaskVo);

	public int insertProcessTaskContent(ProcessTaskContentVo processTaskContentVo);

	public int insertProcessTaskStep(ProcessTaskStepVo processTaskStepVo);

	public int insertProcessTaskStepUser(ProcessTaskStepUserVo processTaskStepUserVo);

	public int insertProcessTaskStepWorkerPolicy(ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo);

	public int insertProcessTaskStepTeam(ProcessTaskStepTeamVo processTaskStepTeamVo);

	public int insertProcessTaskAttribute(ProcessTaskAttributeVo processTaskAttributeVo);

	public int insertProcessTaskStepRel(ProcessTaskStepRelVo processTaskStepRelVo);

	public int insertProcessTaskStepAttribute(ProcessTaskStepAttributeVo processTaskStepAttributeVo);

	public int insertProcessTaskStepContent(@Param("processTaskStepId") Long processTaskStepId, @Param("contentId") Long contentId);

	public int insertProcessTaskStepAuditAttributeData(ProcessTaskStepAuditAttributeDataVo processTaskStepAuditAttributeDataVo);

	public int insertProcessTaskStepWorker(ProcessTaskStepWorkerVo processTaskStepWorkerVo);

	public int insertProcessTaskConverge(ProcessTaskConvergeVo processTaskConvergeVo);

	public int insertProcessTaskAttributeValue(ProcessTaskAttributeValueVo processTaskAttributeValueVo);

	public int insertProcessTaskFormAttributeValue(ProcessTaskAttributeValueVo processTaskAttributeValueVo);

	public int insertProcessTaskStepTimeoutPolicy(ProcessTaskStepTimeoutPolicyVo processTaskStepTimeoutPolicy);

	public int insertProcessTaskStepConfig(@Param("processTaskStepId") Long processTaskStepId, @Param("config") String config);

	public int insertProcessTaskStepFormAttribute(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	public int replaceProcessTaskAttributeData(ProcessTaskAttributeDataVo processTaskAttributeDataVo);

	public int replaceProcessTaskFormAttributeData(ProcessTaskAttributeDataVo processTaskAttributeDataVo);

	public int updateProcessTaskStepExpireTime(ProcessTaskStepVo processTaskStepVo);

	public int updateProcessTaskStepStatus(ProcessTaskStepVo processTaskStepVo);

	public int updateProcessTaskStepRelIsHit(@Param("fromProcessTaskStepId") Long fromProcessTaskStepId, @Param("toProcessTaskStepId") Long toProcessTaskStepId, @Param("isHit") Integer isHit);

	public int updateProcessTaskStepConvergeIsCheck(@Param("isCheck") Integer isCheck, @Param("convergeId") Long convergeId, @Param("processTaskStepId") Long processTaskStepId);

	public int updateProcessTaskStepUserStatus(ProcessTaskStepUserVo processTaskStepUserVo);

	public int deleteProcessTaskAttributeValueByProcessTaskIdAndAttributeUuid(@Param("processTaskId") Long processTaskId, @Param("attributeUuid") String attributeUuid);

	public int deleteProcessTaskFormAttributeValueByProcessTaskIdAndAttributeUuid(@Param("processTaskId") Long processTaskId, @Param("attributeUuid") String attributeUuid);

	public int deleteProcessTaskStepWorker(ProcessTaskStepWorkerVo processTaskStepWorkerVo);
	
	public int deleteOtherProcessTaskStepWorker(String userId);
}
