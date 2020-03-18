package codedriver.module.process.service;

import codedriver.module.process.dto.ProcessStepHandlerVo;

import java.util.List;

public interface ProcessStepHandlerService {
    public List<ProcessStepHandlerVo> searchProcessComponent(String name);

    public void saveStepHandlerConfig(ProcessStepHandlerVo stepHandlerVo);
}
