package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.ChannelProcessVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepRelVo;
import codedriver.module.process.dto.ProcessStepTeamVo;
import codedriver.module.process.dto.ProcessStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessStepUserVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTypeVo;
import codedriver.module.process.dto.ProcessVo;

public interface ProcessMapper {
	public int checkProcessIsExists(String processUuid);

	public ProcessFormVo getProcessFormByProcessUuid(String processUuid);

	public List<ProcessStepRelVo> getProcessStepRelByProcessUuid(String processUuid);

	public List<ProcessStepVo> getProcessStepDetailByProcessUuid(String processUuid);

	public List<ProcessStepFormAttributeVo> getProcessStepFormAttributeByStepUuid(ProcessStepFormAttributeVo processStepFormAttributeVo);

	public ProcessVo getProcessByUuid(String processUuid);

	public ProcessVo getProcessBaseInfoByUuid(String processUuid);

	public List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo);

	public List<ProcessTypeVo> getAllProcessType();

	public int checkProcessNameIsRepeat(ProcessVo processVo);

	public int searchProcessCount(ProcessVo processVo);

	public List<ProcessVo> searchProcessList(ProcessVo processVo);

	public int getProcessReferenceCount(String processUuid);

	public List<ChannelVo> getProcessReferenceList(ChannelProcessVo channelProcessVo);

	public int insertProcess(ProcessVo processVo);

	public int insertProcessStep(ProcessStepVo processStepVo);

	public int insertProcessStepFormAttribute(ProcessStepFormAttributeVo processStepFormAttributeVo);

	public int insertProcessStepRel(ProcessStepRelVo processStepRelVo);

	public int insertProcessStepUser(ProcessStepUserVo processStepUserVo);

	public int insertProcessStepTeam(ProcessStepTeamVo processStepTeamVo);

	public int insertProcessStepTimeoutPolicy(ProcessStepTimeoutPolicyVo processStepTimeoutPolicyVo);

	public int insertProcessStepWorkerPolicy(ProcessStepWorkerPolicyVo processStepWorkerPolicyVo);

	public int insertProcessForm(ProcessFormVo processFormVo);

	public int updateProcess(ProcessVo processVo);

	public int deleteProcessStepByProcessUuid(String processUuid);

	public int deleteProcessStepRelByProcessUuid(String processUuid);

	public int deleteProcessStepUserByProcessUuid(String processUuid);

	public int deleteProcessStepTeamByProcessUuid(String processUuid);

	public int deleteProcessStepWorkerPolicyByProcessUuid(String processUuid);

	public int deleteProcessStepTimeoutPolicyByProcessUuid(String processUuid);

	public int deleteProcessStepFormAttributeByProcessUuid(String processUuid);

	public int deleteProcessByUuid(String uuid);

	public int deleteProcessFormByProcessUuid(String processUuid);

}
