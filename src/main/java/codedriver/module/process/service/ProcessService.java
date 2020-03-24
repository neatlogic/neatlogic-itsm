package codedriver.module.process.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessVo;

public interface ProcessService {
	public ProcessVo getProcessByUuid(String processUuid);

	public ProcessStepVo getProcessStartStep(String processUuid);

	public List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo);

	@Transactional
	public int saveProcess(ProcessVo processVo);

	public ProcessFormVo getProcessFormByProcessUuid(String processUuid);

	public List<ProcessStepFormAttributeVo> getProcessStepFormAttributeByStepUuid(ProcessStepFormAttributeVo processStepFormAttributeVo);

}
