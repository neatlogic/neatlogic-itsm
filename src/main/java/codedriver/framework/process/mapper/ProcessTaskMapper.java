package codedriver.framework.process.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.process.dto.ProcessTaskAssignUserVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskAttributeValueVo;
import codedriver.framework.process.dto.ProcessTaskAttributeVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskConvergeVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepTeamVo;
import codedriver.framework.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;

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
}
