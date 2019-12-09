package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.ProcessTaskStepAttributeVo;

public interface ProcessTaskAttributeService {
	public List<ProcessTaskStepAttributeVo> getProcessTaskStepAttributeListByProcessTaskStepId(Long processTaskId, Long processTaskStepId);
}
