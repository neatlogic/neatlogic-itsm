package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.constvalue.AttributeHandler;
import codedriver.framework.attribute.constvalue.AttributeType;

public class ProcessTaskStepFormAttributeVo {
	private Long processTaskId;
	private Long processTaskStepId;
	private String action;
	private Integer isEditable;
	private Integer isRequired;
	private String formUuid;
	private String processStepUuid;
	private String attributeUuid;
	private String config;
	private String data;
	private String label;
	private String handler;
	private String handlerName;
	private String type;
	private String typeName;
	private ProcessTaskFormAttributeDataVo attributeData;
	private JSONObject configObj;

	public ProcessTaskStepFormAttributeVo() {

	}

	public ProcessTaskStepFormAttributeVo(Long _processTaskStepId, String _attributeUuid) {
		this.processTaskStepId = _processTaskStepId;
		this.attributeUuid = _attributeUuid;
	}

	public ProcessTaskStepFormAttributeVo(Long _processTaskStepId) {
		this.processTaskStepId = _processTaskStepId;
	}

	public ProcessTaskStepFormAttributeVo(ProcessStepFormAttributeVo processStepFormAttributeVo) {
		this.setIsEditable(processStepFormAttributeVo.getIsEditable());
		this.setProcessStepUuid(processStepFormAttributeVo.getProcessStepUuid());
		this.setAttributeUuid(processStepFormAttributeVo.getAttributeUuid());
		this.setLabel(processStepFormAttributeVo.getLabel());
		this.setConfig(processStepFormAttributeVo.getConfig());
		this.setHandler(processStepFormAttributeVo.getHandler());
		this.setType(processStepFormAttributeVo.getType());
		this.setAction(processStepFormAttributeVo.getAction());
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

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

	public void setConfig(String config) {
		this.config = config;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public JSONObject getConfigObj() {
		if (configObj == null && StringUtils.isNotBlank(config)) {
			try {
				configObj = JSONObject.parseObject(config);
			} catch (Exception ex) {

			}
		}
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public String getHandlerName() {
		if (StringUtils.isNotBlank(handler) && StringUtils.isBlank(handlerName)) {
			handlerName = AttributeHandler.getName(handler);
		}
		return handlerName;
	}

	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
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

	public ProcessTaskFormAttributeDataVo getAttributeData() {
		return attributeData;
	}

	public void setAttributeData(ProcessTaskFormAttributeDataVo attributeData) {
		this.attributeData = attributeData;
	}

	public String getConfig() {
		return config;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
