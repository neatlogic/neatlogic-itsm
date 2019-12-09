package codedriver.framework.process.mapper;

import java.util.List;

import codedriver.framework.process.dto.ProcessStepHandlerVo;

public interface ProcessStepHandlerMapper {
	public List<ProcessStepHandlerVo> getActiveProcessStepHandler();

	public int resetProcessStepHandler();

	public int replaceProcessStepHandler(ProcessStepHandlerVo processStepHandlerVo);
}
