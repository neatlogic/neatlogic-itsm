package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;

public interface ProcessTaskService {
	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public List<ProcessTaskStepAttributeVo> getProcessTaskStepAttributeByStepId(ProcessTaskStepAttributeVo processTaskStepAttributeVo);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

}
