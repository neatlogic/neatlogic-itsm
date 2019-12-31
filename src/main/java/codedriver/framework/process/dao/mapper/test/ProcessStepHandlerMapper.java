package codedriver.framework.process.dao.mapper.test;

import java.util.List;

import codedriver.module.process.dto.ProcessStepHandlerVo;

public interface ProcessStepHandlerMapper {
	public List<ProcessStepHandlerVo> getActiveProcessStepHandler();

	public int resetProcessStepHandler();

	public int replaceProcessStepHandler(ProcessStepHandlerVo processStepHandlerVo);
}
