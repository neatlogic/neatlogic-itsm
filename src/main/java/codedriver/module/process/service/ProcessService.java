package codedriver.module.process.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessStepAttributeVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessVo;

public interface ProcessService {
	public ProcessVo getProcessByUuid(String processUuid);

	public ProcessStepVo getProcessStartStep(String processUuid);

	public List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo);

	public List<ProcessStepAttributeVo> getProcessStepAttributeByStepUuid(ProcessStepAttributeVo processStepAttributeVo);

	@Transactional
	public int saveProcess(ProcessVo processVo);

	public ProcessFormVo getProcessFormByProcessUuid(String processUuid);

	public List<ProcessStepFormAttributeVo> getProcessStepFormAttributeByStepUuid(ProcessStepFormAttributeVo processStepFormAttributeVo);

}
