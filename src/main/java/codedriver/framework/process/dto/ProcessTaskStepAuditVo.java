package codedriver.framework.process.dto;

import codedriver.framework.asynchronization.threadlocal.UserContext;

public class ProcessTaskStepAuditVo {
	private Long id;
	private Long processTaskId;
	private Long processTaskStepId;
	private String userId;
	private String userName;
	private String actionTime;
	private String action;
	private String contentJson;
	private String content;
	private Long contentId;

	public ProcessTaskStepAuditVo() {

	}

	public ProcessTaskStepAuditVo(ProcessTaskStepVo processTaskStepVo) {
		this.setProcessTaskId(processTaskStepVo.getProcessTaskId());
		this.setProcessTaskStepId(processTaskStepVo.getId());
		this.setContentId(processTaskStepVo.getContentId());
		this.setUserId(UserContext.get().getUserId());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getActionTime() {
		return actionTime;
	}

	public void setActionTime(String actionTime) {
		this.actionTime = actionTime;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getContent() {

		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

}
