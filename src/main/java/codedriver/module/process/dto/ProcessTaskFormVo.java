package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

public class ProcessTaskFormVo {
	private Long processTaskId;
	private String formUuid;
	private String formName;
	private String formContent;
	private String formContentHash;

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

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFormContent() {
		return formContent;
	}

	public void setFormContent(String formContent) {
		this.formContent = formContent;
	}

	public String getFormContentHash() {
		if (StringUtils.isBlank(formContentHash) && StringUtils.isNotBlank(formContent)) {
			formContentHash = DigestUtils.md5DigestAsHex(formContent.getBytes());
		}
		return formContentHash;
	}

	public void setFormContentHash(String formContentHash) {
		this.formContentHash = formContentHash;
	}

}
