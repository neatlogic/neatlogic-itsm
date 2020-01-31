package codedriver.module.process.dto;

public class ProcessTaskStepTimeAuditVo {
	private Long id;
	private Long processTaskStepId;
	private String activeTime;
	private String startTime;
	private String abortTime;
	private String completeTime;
	private String backTime;
	private Long activeTimeLong;
	private Long startTimeLong;
	private Long abortTimeLong;
	private Long completeTimeLong;
	private Long backTimeLong;

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

	public String getBackTime() {
		return backTime;
	}

	public void setBackTime(String backTime) {
		this.backTime = backTime;
	}

	public Long getActiveTimeLong() {
		return activeTimeLong;
	}

	public void setActiveTimeLong(Long activeTimeLong) {
		this.activeTimeLong = activeTimeLong;
	}

	public Long getStartTimeLong() {
		return startTimeLong;
	}

	public void setStartTimeLong(Long startTimeLong) {
		this.startTimeLong = startTimeLong;
	}

	public Long getAbortTimeLong() {
		return abortTimeLong;
	}

	public void setAbortTimeLong(Long abortTimeLong) {
		this.abortTimeLong = abortTimeLong;
	}


	public Long getBackTimeLong() {
		return backTimeLong;
	}

	public void setBackTimeLong(Long backTimeLong) {
		this.backTimeLong = backTimeLong;
	}

	public String getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}

	public Long getCompleteTimeLong() {
		return completeTimeLong;
	}

	public void setCompleteTimeLong(Long completeTimeLong) {
		this.completeTimeLong = completeTimeLong;
	}

}
