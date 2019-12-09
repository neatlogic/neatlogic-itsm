package codedriver.framework.process.dto;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author:chenqiwei
 * @Time:Jun 19, 2019
 * @ClassName: ProcessTaskStepWorkerVo
 * @Description: 记录当前流程任务谁可以处理
 */
public class ProcessTaskStepWorkerVo {
	private Long processTaskId;
	private Long processTaskStepId;
	private String userId;
	private String userName;
	private String teamUuid;
	private String roleName;
	private String action = "handle";

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof ProcessTaskStepWorkerVo))
			return false;

		final ProcessTaskStepWorkerVo user = (ProcessTaskStepWorkerVo) other;
		try {
			if (getProcessTaskId().equals(user.getProcessTaskId()) && getProcessTaskStepId().equals(user.getProcessTaskStepId()) && ((getUserId() != null && getUserId().equals(user.getUserId())) || (getTeamUuid() != null && getTeamUuid().equals(user.getTeamUuid()))) || (getRoleName() != null && getRoleName().equals(user.getRoleName()))) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = getProcessTaskId().hashCode() * 7 + getProcessTaskStepId().hashCode() * 11;
		if (StringUtils.isNotBlank(getUserId())) {
			result += getUserId().hashCode() * 37;
		} else if (StringUtils.isNotBlank(getRoleName())) {
			result += getRoleName().hashCode() * 37;
		} else if (getTeamUuid() != null) {
			result += getTeamUuid().hashCode() * 37;
		}
		return result;
	}

	public ProcessTaskStepWorkerVo() {

	}

	public ProcessTaskStepWorkerVo(Long _processTaskStepId, String _userId) {
		this.processTaskStepId = _processTaskStepId;
		this.userId = _userId;
	}

	public ProcessTaskStepWorkerVo(Long _processTaskStepId) {
		this.processTaskStepId = _processTaskStepId;
	}

	public ProcessTaskStepWorkerVo(Long _processTaskId, Long _processTaskStepId, String _userId) {
		this.processTaskId = _processTaskId;
		this.processTaskStepId = _processTaskStepId;
		this.userId = _userId;
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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getTeamUuid() {
		return teamUuid;
	}

	public void setTeamUuid(String teamUuid) {
		this.teamUuid = teamUuid;
	}

}
