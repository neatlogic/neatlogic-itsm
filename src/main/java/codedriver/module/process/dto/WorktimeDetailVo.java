package codedriver.module.process.dto;

import java.util.List;

public class WorktimeDetailVo {

	private Integer workYear;
	private String workDate;
	private String  worktimeUuid;
	private Long workStart;
	private Long workEnd;
	private List<String> workDateList;
	public Integer getWorkYear() {
		return workYear;
	}
	public void setWorkYear(Integer workYear) {
		this.workYear = workYear;
	}
	public String getWorkDate() {
		return workDate;
	}
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	public String getWorktimeUuid() {
		return worktimeUuid;
	}
	public void setWorktimeUuid(String worktimeUuid) {
		this.worktimeUuid = worktimeUuid;
	}
	public Long getWorkStart() {
		return workStart;
	}
	public void setWorkStart(Long workStart) {
		this.workStart = workStart;
	}
	public Long getWorkEnd() {
		return workEnd;
	}
	public void setWorkEnd(Long workEnd) {
		this.workEnd = workEnd;
	}
	public List<String> getWorkDateList() {
		return workDateList;
	}
	public void setWorkDateList(List<String> workDateList) {
		this.workDateList = workDateList;
	}
}
