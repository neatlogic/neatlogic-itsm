package codedriver.module.process.service.test;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public interface ProcessTaskService1 {

	List<ProcessTaskStepRelVo> getProcessTaskStepRelByProcessTaskId(Long processTaskId);

	List<ProcessTaskStepVo> getProcessTaskStepStatusByFlowJobId(Long flowJobId);

	ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

}
