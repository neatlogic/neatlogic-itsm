package codedriver.module.process.dto;

public class WorktimeDefineVo {

	private String worktimeUuid;
	private String startTime;
	private String endTime;
	private Integer weekday;

	public String getWorktimeUuid() {
		return worktimeUuid;
	}
	public void setWorktimeUuid(String worktimeUuid) {
		this.worktimeUuid = worktimeUuid;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getWeekday() {
		return weekday;
	}
	public void setWeekday(Integer weekday) {
		this.weekday = weekday;
	}
	
}
