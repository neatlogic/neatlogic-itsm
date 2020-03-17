package codedriver.module.process.dto;

import java.util.List;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskStepAuditVo {
	@EntityField(name = "活动id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "工单id", type = ApiParamType.LONG)
	private Long processTaskId;
	@EntityField(name = "步骤id", type = ApiParamType.LONG)
	private Long processTaskStepId;
	@EntityField(name = "步骤名称", type = ApiParamType.STRING)
	private String processTaskStepName;
	@EntityField(name = "用户userId", type = ApiParamType.STRING)
	private String userId;
	@EntityField(name = "用户名", type = ApiParamType.STRING)
	private String userName;
	@EntityField(name = "创建时间", type = ApiParamType.LONG)
	private String actionTime;
	@EntityField(name = "活动类型，startprocess(上报)、complete(完成)、retreat(撤回)、abort(终止)、recover(恢复)、transfer(转交)、updateTitle(更新标题)、updatePriority(更新优先级)、updateContent(更新上报描述内容)、comment(评论)", type = ApiParamType.STRING)
	private String action;
	//@EntityField(name = "活动详情列表", type = ApiParamType.JSONARRAY)
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
