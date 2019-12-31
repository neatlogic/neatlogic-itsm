package codedriver.framework.process.dao.mapper.test;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public interface ProcessTaskMapper1 {

	List<ProcessTaskStepRelVo> getProcessTaskStepRelByProcessTaskId(Long processTaskId);
	
	public List<ProcessTaskStepVo> getProcessTaskStepStatusByProcessTaskId(Long processTaskId);
	
	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);	
	
}
