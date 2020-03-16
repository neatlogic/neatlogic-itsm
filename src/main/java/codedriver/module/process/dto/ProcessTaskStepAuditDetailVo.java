package codedriver.module.process.dto;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskStepAuditDetailVo {
	@EntityField(name = "活动id", type = ApiParamType.LONG)
	private Long auditId;
	@EntityField(name = "详情类型，title(标题)、priority(优先级)、content(内容)、worker(处理人)、file(上传文件)", type = ApiParamType.STRING)
	private String type;
	@EntityField(name = "旧内容", type = ApiParamType.STRING)
	private String oldContent;
	@EntityField(name = "新内容", type = ApiParamType.STRING)
	private String newContent;

	public ProcessTaskStepAuditDetailVo() {

	}

	public ProcessTaskStepAuditDetailVo(Long _auditId, String _type, String _oldContent, String _newContent) {
		auditId = _auditId;
		type = _type;
		oldContent = _oldContent;
		newContent = _newContent;
	}

	public Long getAuditId() {
		return auditId;
	}

	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOldContent() {
		return oldContent;
	}

	public void setOldContent(String oldContent) {
		this.oldContent = oldContent;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

}
