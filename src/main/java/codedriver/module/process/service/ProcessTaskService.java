package codedriver.module.process.service;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepVo;

public interface ProcessTaskService {
	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public List<ProcessTaskStepAttributeVo> getProcessTaskStepAttributeByStepId(ProcessTaskStepAttributeVo processTaskStepAttributeVo);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

}
