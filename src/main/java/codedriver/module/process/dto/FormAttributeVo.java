package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.List;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class FormAttributeVo implements Serializable {
	private static final long serialVersionUID = 8282018124626035430L;
	@EntityField(name = "属性uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "表单uuid", type = ApiParamType.STRING)
	private String formUuid;
	@EntityField(name = "表单版本uuid", type = ApiParamType.STRING)
	private String formVersionUuid;
	@EntityField(name = "属性标签名", type = ApiParamType.STRING)
	private String label;
	@EntityField(name = "类型", type = ApiParamType.STRING)
	private String type;
	@EntityField(name = "处理器", type = ApiParamType.STRING)
	private String handler;
	@EntityField(name = "属性配置", type = ApiParamType.STRING)
	private String config;
	@EntityField(name = "属性数据", type = ApiParamType.STRING)
	private String data;
	@EntityField(name = "是否必填", type = ApiParamType.BOOLEAN)
	private boolean isRequired;
	@EntityField(name = "表达式列表", type = ApiParamType.JSONARRAY)
	List<ProcessExpressionVo> expressionList;
	
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

	public FormAttributeVo(String formUuid, String formVersionUuid, String uuid, String label, String type, String handler, boolean isRequired, String config, String data) {
		this.uuid = uuid;
		this.formUuid = formUuid;
		this.formVersionUuid = formVersionUuid;
		this.label = label;
		this.type = type;
		this.handler = handler;
		this.isRequired = isRequired;
		this.config = config;
		this.data = data;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public List<ProcessExpressionVo> getExpressionList() {
		return expressionList;
	}

	public void setExpressionList(List<ProcessExpressionVo> expressionList) {
		this.expressionList = expressionList;
	}

}
