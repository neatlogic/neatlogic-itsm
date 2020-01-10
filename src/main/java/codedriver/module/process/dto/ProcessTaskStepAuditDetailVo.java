package codedriver.module.process.dto;

public class ProcessTaskStepAuditDetailVo {
	private Long auditId;
	private String type;
	private String oldContent;
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
