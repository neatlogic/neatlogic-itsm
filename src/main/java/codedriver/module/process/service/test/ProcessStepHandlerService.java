package codedriver.module.process.service.test;

import java.util.List;

import codedriver.module.process.dto.ProcessStepHandlerVo;

public interface ProcessStepHandlerService {
	public List<ProcessStepHandlerVo> getActiveProcessStepHandler();
}
