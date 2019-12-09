package codedriver.module.process.dto;

import codedriver.framework.attribute.dto.AttributeDataVo;

public class ProcessTaskAttributeDataVo extends AttributeDataVo {
	private Long processTaskId;
	private Long processTaskStepId;
	private String formUuid;

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

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

}
