package codedriver.module.process.dto;

public class ProcessTaskStepAuditFormAttributeDataVo {
	private Long auditId;
	private Long processTaskId;
	private Long processTaskStepId;
	private String attributeUuid;
	private String formUuid;
	private String data;

	public ProcessTaskStepAuditFormAttributeDataVo() {

	}

	public ProcessTaskStepAuditFormAttributeDataVo(ProcessTaskAttributeDataVo data) {
		this.processTaskId = data.getProcessTaskId();
		this.processTaskStepId = data.getProcessTaskStepId();
		this.attributeUuid = data.getAttributeUuid();
		this.formUuid = data.getFormUuid();
		this.data = data.getData();
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public Long getAuditId() {
		return auditId;
	}

	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

}
