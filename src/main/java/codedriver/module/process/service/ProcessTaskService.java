package codedriver.module.process.service;

import java.util.List;
import java.util.Set;

import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;

public interface ProcessTaskService {
	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	public List<String> getProcessTaskStepActionList(Long processTaskId, Long processTaskStepId);
	
	public boolean verifyActionAuthoriy(Long processTaskId, Long processTaskStepId, ProcessTaskStepAction action);
	
	public Set<ProcessTaskStepVo> getRetractableStepListByProcessTaskId(Long processTaskId);
	
	public List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId);
}
