package codedriver.module.process.dto;

import java.util.List;

public class ProcessTaskStepAuditVo {
	private Long id;
	private Long processTaskId;
	private Long processTaskStepId;
	private String processTaskStepName;
	private String userId;
	private String userName;
	private String actionTime;
	private String action;
	private List<ProcessTaskStepAuditDetailVo> auditDetailList;

	public ProcessTaskStepAuditVo() { 
	}
	
	public ProcessTaskStepAuditVo(Long _processTaskId,String _action) { 
		this.processTaskId = _processTaskId;
		this.action = _action;
	}

	public ProcessTaskStepAuditVo(Long processTaskId, Long processTaskStepId, String userId, String action) {
		this.processTaskId = processTaskId;
		this.processTaskStepId = processTaskStepId;
		this.userId = userId;
		this.action = action;
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

	public String getProcessTaskStepName() {
		return processTaskStepName;
	}

	public void setProcessTaskStepName(String processTaskStepName) {
		this.processTaskStepName = processTaskStepName;
	}

	public String getUserId() {
//		if (StringUtils.isBlank(userId)) {
//			userId = UserContext.get().getUserId();
//		}
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

	public List<ProcessTaskStepAuditDetailVo> getAuditDetailList() {
		return auditDetailList;
	}

	public void setAuditDetailList(List<ProcessTaskStepAuditDetailVo> auditDetailList) {
		this.auditDetailList = auditDetailList;
	}

}
