package codedriver.module.process.dto;

import java.io.Serializable;

public class FormAttributeVo implements Serializable {
	private static final long serialVersionUID = 8282018124626035430L;
	private String uuid;
	private String formUuid;
	private String formVersionUuid;
	private String label;
	private String type;
	private String handler;
	private String config;

	public FormAttributeVo() {

	}

//	public FormAttributeVo(String _formUuid, String _formVersionUuid, String _uuid, String _config) {
//		this.formUuid = _formUuid;
//		this.formVersionUuid = _formVersionUuid;
//		this.uuid = uuid;
//		this.config = _config;
//	}
	
	public FormAttributeVo(String formUuid) {
		this.formUuid = formUuid;
	}
	
	public FormAttributeVo(String formUuid, String formVersionUuid) {
		this.formUuid = formUuid;
		this.formVersionUuid = formVersionUuid;
	}

	public FormAttributeVo(String formUuid, String formVersionUuid, String uuid, String label, String type, String handler, String config) {
		this.uuid = uuid;
		this.formUuid = formUuid;
		this.formVersionUuid = formVersionUuid;
		this.label = label;
		this.type = type;
		this.handler = handler;
		this.config = config;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getFormVersionUuid() {
		return formVersionUuid;
	}

	public void setFormVersionUuid(String formVersionUuid) {
		this.formVersionUuid = formVersionUuid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
