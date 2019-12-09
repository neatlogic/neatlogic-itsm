package codedriver.framework.process.dto;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.AttributeHandlerFactory;
import codedriver.framework.process.attribute.core.IAttributeHandler;
import codedriver.module.process.constvalue.AttributeHandler;
import codedriver.module.process.constvalue.AttributeType;

public class ProcessStepFormAttributeVo implements Serializable {
	private static final long serialVersionUID = -6435866167443319573L;
	private String processUuid;
	private String processStepUuid;
	private String formUuid;
	private String attributeUuid;
	private Integer isEditable;
	private Integer isRequired;
	private String config;
	private String data;
	private String label;
	private String name;
	private String help;
	private String unit;
	private String handler;
	private String handlerName;
	private String type;
	private String typeName;
	private String dataCubeTextField;
	private String dataCubeValueField;
	private String dataCubeUuid;
	private String description;
	private String inputPage;
	private String viewPage;
	private String configPage;
	private String editTemplate;
	private String viewTemplate;
	private String configTemplate;

	public ProcessStepFormAttributeVo() {

	}

	public ProcessStepFormAttributeVo(String _processStepUuid, String _attributeUuid) {
		this.processStepUuid = _processStepUuid;
		this.attributeUuid = _attributeUuid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof ProcessStepFormAttributeVo))
			return false;

		final ProcessStepFormAttributeVo attribute = (ProcessStepFormAttributeVo) other;
		try {
			if (getAttributeUuid().equals(attribute.getAttributeUuid())) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = getAttributeUuid().hashCode() * 29;
		return result;
	}

	private JSONObject configObj;

	public Integer getIsEditable() {
		return isEditable;
	}

	public void setIsEditable(Integer isEditable) {
		this.isEditable = isEditable;
	}

	public Integer getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(Integer isRequired) {
		this.isRequired = isRequired;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public JSONObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getProcessStepUuid() {
		return processStepUuid;
	}

	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataCubeTextField() {
		return dataCubeTextField;
	}

	public void setDataCubeTextField(String dataCubeTextField) {
		this.dataCubeTextField = dataCubeTextField;
	}

	public String getDataCubeValueField() {
		return dataCubeValueField;
	}

	public void setDataCubeValueField(String dataCubeValueField) {
		this.dataCubeValueField = dataCubeValueField;
	}

	public String getDataCubeUuid() {
		return dataCubeUuid;
	}

	public void setDataCubeUuid(String dataCubeUuid) {
		this.dataCubeUuid = dataCubeUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInputPage() {
		if (StringUtils.isBlank(inputPage) && StringUtils.isNotBlank(handler)) {
			IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(handler);
			if (attributeHandler != null) {
				inputPage = attributeHandler.getInputPage();
			}
		}
		return inputPage;
	}

	public void setInputPage(String inputPage) {
		this.inputPage = inputPage;
	}

	public String getViewPage() {
		if (StringUtils.isBlank(viewPage) && StringUtils.isNotBlank(handler)) {
			IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(handler);
			if (attributeHandler != null) {
				viewPage = attributeHandler.getViewPage();
			}
		}
		return viewPage;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public void setViewPage(String viewPage) {
		this.viewPage = viewPage;
	}

	public String getEditTemplate() {
		return editTemplate;
	}

	public void setEditTemplate(String editTemplate) {
		this.editTemplate = editTemplate;
	}

	public String getViewTemplate() {
		return viewTemplate;
	}

	public void setViewTemplate(String viewTemplate) {
		this.viewTemplate = viewTemplate;
	}

	public String getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(String configTemplate) {
		this.configTemplate = configTemplate;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getConfigPage() {
		if (StringUtils.isBlank(configPage) && StringUtils.isNotBlank(handler)) {
			IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(handler);
			if (attributeHandler != null) {
				configPage = attributeHandler.getConfigPage();
			}
		}
		return configPage;
	}

	public String getHandlerName() {
		if (StringUtils.isNotBlank(handler) && StringUtils.isBlank(handlerName)) {
			handlerName = AttributeHandler.getName(handler);
		}
		return handlerName;
	}

	public void setConfigPage(String configPage) {
		this.configPage = configPage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTypeName() {
		if (StringUtils.isNotBlank(type) && StringUtils.isBlank(typeName)) {
			typeName = AttributeType.getName(type);
		}
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

}
