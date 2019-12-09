package codedriver.module.process.dto;

public class ProcessTaskAssignUserVo {
	private Long processTaskId;
	private Long fromStepId;
	private Long toStepId;
	private String userId;
	private String assignTime;

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public Long getFromStepId() {
		return fromStepId;
	}

	public void setFromStepId(Long fromStepId) {
		this.fromStepId = fromStepId;
	}

	public Long getToStepId() {
		return toStepId;
	}

	public void setToStepId(Long toStepId) {
		this.toStepId = toStepId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(String assignTime) {
		this.assignTime = assignTime;
	}

}
