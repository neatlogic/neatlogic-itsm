package codedriver.framework.process.stephandler.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.mapper.ProcessTaskMapper;

@Service
public class ProcessTaskStepTimeoutDetector extends Thread {
	private Boolean isDone = false;
	private Long timecost;
	private Long processTaskStepId;
	private static ProcessTaskMapper processTaskMapper;

	@Autowired
	public void setProcessTaskMapper(ProcessTaskMapper processTaskMapper) {
		processTaskMapper = processTaskMapper;
	}

	public ProcessTaskStepTimeoutDetector() {

	}

	public ProcessTaskStepTimeoutDetector(Long _processTaskStepId, Long _timecost) {
		this.timecost = _timecost;
		this.processTaskStepId = _processTaskStepId;
		this.setDaemon(true);
	}

	@Override
	public void run() {
		try {
			this.setName("PROCESSTASK-TIMEOUT-DETECTOR-" + processTaskStepId);
			Thread.sleep(timecost);
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			
		} catch (Exception e) {

		}
	}

}
