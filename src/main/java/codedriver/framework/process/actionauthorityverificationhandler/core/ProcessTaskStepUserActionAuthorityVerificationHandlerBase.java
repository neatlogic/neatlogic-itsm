package codedriver.framework.process.actionauthorityverificationhandler.core;

import org.springframework.beans.factory.annotation.Autowired;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public abstract class ProcessTaskStepUserActionAuthorityVerificationHandlerBase implements IProcessTaskStepUserActionAuthorityVerificationHandler {

	protected static ProcessTaskMapper processTaskMapper;
	
	@Autowired
	public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
		processTaskMapper = _processTaskMapper;
	}
	
	@Override
	public boolean test(Long processTaskId, Long processTaskStepId) {
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		//获取步骤信息
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
			throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'工单：'" + processTaskId + "'的步骤");
		}
		return myTest(processTaskVo, processTaskStepVo);
	}

	protected abstract boolean myTest(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo);
}
