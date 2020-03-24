package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.ProcessStepHandlerVo;

public interface ProcessStepHandlerService {
    public List<ProcessStepHandlerVo> searchProcessComponent(String name);

    public void saveStepHandlerConfig(ProcessStepHandlerVo stepHandlerVo);
}
