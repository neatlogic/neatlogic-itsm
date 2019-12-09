package codedriver.module.process.dto;

public class ProcessTaskConvergeVo {
	private Long convergeId;
	private Long processTaskId;
	private Long processTaskStepId;

	public ProcessTaskConvergeVo(Long _procssTaskId, Long _processTaskStepId, Long _convergeId) {
		processTaskId = _procssTaskId;
		processTaskStepId = _processTaskStepId;
		convergeId = _convergeId;
	}

	public Long getConvergeId() {
		return convergeId;
	}

	public void setConvergeId(Long convergeId) {
		this.convergeId = convergeId;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

}
