package codedriver.module.process.dto;

import java.util.List;

public class WorktimeRangeVo {

	private Integer year;
	private String date;
	private String worktimeUuid;
	private Long startTime;
	private Long endTime;
	private List<String> dateList;

	public WorktimeRangeVo() {

	}

	public WorktimeRangeVo(Long _startTime, Long _endTime) {
		startTime = _startTime;
		endTime = _endTime;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getWorktimeUuid() {
		return worktimeUuid;
	}

	public void setWorktimeUuid(String worktimeUuid) {
		this.worktimeUuid = worktimeUuid;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public List<String> getDateList() {
		return dateList;
	}

	public void setDateList(List<String> dateList) {
		this.dateList = dateList;
	}

}
