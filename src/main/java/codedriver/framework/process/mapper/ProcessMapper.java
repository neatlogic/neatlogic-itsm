package codedriver.framework.process.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.process.dto.ProcessAttributeVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessStepAttributeVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepRelVo;
import codedriver.framework.process.dto.ProcessStepTeamVo;
import codedriver.framework.process.dto.ProcessStepTimeoutPolicyVo;
import codedriver.framework.process.dto.ProcessStepUserVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessVo;

public interface ProcessMapper {
	public int checkProcessIsExists(String processUuid);

	public ProcessFormVo getProcessFormByProcessUuid(String processUuid);

	public List<ProcessStepRelVo> getProcessStepRelByProcessUuid(String processUuid);

	public List<ProcessStepVo> getProcessStepDetailByProcessUuid(String processUuid);

	public List<ProcessStepAttributeVo> getProcessStepAttributeByStepUuid(ProcessStepAttributeVo processStepAttributeVo);

	public List<ProcessStepFormAttributeVo> getProcessStepFormAttributeByStepUuid(ProcessStepFormAttributeVo processStepFormAttributeVo);
	
	public ProcessVo getProcessByUuid(String processUuid);

	public ProcessVo getProcessBaseInfoByUuid(String processUuid);

	public List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo);

	public int replaceProcess(ProcessVo processVo);

	public int insertProcessStep(ProcessStepVo processStepVo);

	public int insertProcessAttribute(ProcessAttributeVo processAttributeVo);

	public int insertProcessStepFormAttribute(ProcessStepFormAttributeVo processStepFormAttributeVo);

	public int insertProcessStepRel(ProcessStepRelVo processStepRelVo);

	public int insertProcessStepUser(ProcessStepUserVo processStepUserVo);

	public int insertProcessStepTeam(ProcessStepTeamVo processStepTeamVo);

	public int insertProcessStepTimeoutPolicy(ProcessStepTimeoutPolicyVo processStepTimeoutPolicyVo);

	public int insertProcessStepWorkerPolicy(ProcessStepWorkerPolicyVo processStepWorkerPolicyVo);

	public int insertProcessStepAttribute(ProcessStepAttributeVo processStepAttributeVo);

	public int replaceProcessForm(@Param("processUuid") String processUuid, @Param("formUuid") String formUuid);

	public int deleteProcessStepByProcessUuid(String processUuid);

	public int deleteProcessStepRelByProcessUuid(String processUuid);

	public int deleteProcessStepUserByProcessUuid(String processUuid);

	public int deleteProcessAttributeByProcessUuid(String processUuid);

	public int deleteProcessStepTeamByProcessUuid(String processUuid);

	public int deleteProcessStepWorkerPolicyByProcessUuid(String processUuid);

	public int deleteProcessStepAttributeByProcessUuid(String processUuid);

	public int deleteProcessStepTimeoutPolicyByProcessUuid(String processUuid);

	public int deleteProcessStepFormAttributeByProcessUuid(String processUuid);
}
