package codedriver.module.process.dto;

public class ProcessTaskStepTimeAuditVo {
	private Long id;
	private Long processTaskStepId;
	private String activeTime;
	private String startTime;
	private String abortTime;
	private String successTime;
	private String failedTime;
	private String backTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

	public String getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(String activeTime) {
		this.activeTime = activeTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getAbortTime() {
		return abortTime;
	}

	public void setAbortTime(String abortTime) {
		this.abortTime = abortTime;
	}

	public String getSuccessTime() {
		return successTime;
	}

	public void setSuccessTime(String successTime) {
		this.successTime = successTime;
	}

	public String getFailedTime() {
		return failedTime;
	}

	public void setFailedTime(String failedTime) {
		this.failedTime = failedTime;
	}

	public String getBackTime() {
		return backTime;
	}

	public void setBackTime(String backTime) {
		this.backTime = backTime;
	}

}
