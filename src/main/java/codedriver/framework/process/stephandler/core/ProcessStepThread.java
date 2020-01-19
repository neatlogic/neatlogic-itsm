package codedriver.framework.process.stephandler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.module.process.dto.ProcessTaskStepVo;

public abstract class ProcessStepThread extends CodeDriverThread {
	Logger logger = LoggerFactory.getLogger(ProcessStepThread.class);
	private ProcessTaskStepVo processTaskStepVo;

	public ProcessTaskStepVo getProcessTaskStepVo() {
		return processTaskStepVo;
	}

	public ProcessStepThread(ProcessTaskStepVo _processTaskStepVo) {
		this.processTaskStepVo = _processTaskStepVo;
		if (_processTaskStepVo != null) {
			this.setThreadName("PROCESSTASK-STEP-HANDLER-" + _processTaskStepVo.getId());
		}
	}
}
