package codedriver.module.process.service;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public interface ProcessTaskService {
	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

}
