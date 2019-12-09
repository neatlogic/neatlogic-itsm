package codedriver.module.process.dto;

import java.io.Serializable;

public class FormAttributeVo implements Serializable {
	private static final long serialVersionUID = 8282018124626035430L;
	private String attributeUuid;
	private String formUuid;
	private String formVersionUuid;
	private String config;

	public FormAttributeVo() {

	}

	public FormAttributeVo(String _formUuid, String _formVersionUuid, String _attributeUuid, String _config) {
		this.formUuid = _formUuid;
		this.formVersionUuid = _formVersionUuid;
		this.attributeUuid = _attributeUuid;
		this.config = _config;
	}

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
