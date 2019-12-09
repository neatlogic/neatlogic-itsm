package codedriver.framework.process.dto;

import org.apache.commons.lang3.StringUtils;

import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepUserStatus;

public class ProcessTaskStepUserVo {
	private Long processTaskId;
	private Long processTaskStepId;
	private String userId;
	private String userName;
	private String userType = "major";
	private String status = ProcessTaskStepUserStatus.DOING.getValue();
	private String statusName;
	private String startTime;
	private String endTime;
	private Long timeCost;
	private String timeCostStr;

	public ProcessTaskStepUserVo() {

	}

	public ProcessTaskStepUserVo(Long _processTaskStepId, String _userId) {
		this.setProcessTaskStepId(_processTaskStepId);
		this.setUserId(_userId);
	}

	public ProcessTaskStepUserVo(Long _processTaskStepId, String _userId, String _status) {
		this.setProcessTaskStepId(_processTaskStepId);
		this.setUserId(_userId);
		this.setStatus(_status);
	}

	public ProcessTaskStepUserVo(Long _processTaskId, Long _processTaskStepId, String _userId, String _status) {
		this.setProcessTaskId(_processTaskId);
		this.setProcessTaskStepId(_processTaskStepId);
		this.setUserId(_userId);
		this.setStatus(_status);
	}

	public ProcessTaskStepUserVo(ProcessStepUserVo processStepUserVo) {
		this.setUserId(processStepUserVo.getUserId());
		this.setUserName(processStepUserVo.getUserName());
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		if (StringUtils.isNotBlank(status) && StringUtils.isBlank(statusName)) {
			statusName = ProcessTaskStatus.getText(status);
		}
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
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

	public Long getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(Long timeCost) {
		this.timeCost = timeCost;
	}

	public String getTimeCostStr() {
		return timeCostStr;
	}

	public void setTimeCostStr(String timeCostStr) {
		this.timeCostStr = timeCostStr;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

}
