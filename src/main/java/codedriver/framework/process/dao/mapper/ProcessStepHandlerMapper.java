package codedriver.framework.process.dao.mapper;

import codedriver.module.process.dto.ProcessStepHandlerVo;

import java.util.List;

public interface ProcessStepHandlerMapper {

    public List<ProcessStepHandlerVo> getProcessStepHandlerConfig();

    public void updateProcessStepHandlerConfig(ProcessStepHandlerVo stepHandlerVo);

    public void deleteProcessStepHandlerConfigByHandler(String handler);
}
