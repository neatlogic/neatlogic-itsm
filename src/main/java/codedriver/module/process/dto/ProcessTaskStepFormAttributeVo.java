package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.constvalue.AttributeHandler;
import codedriver.framework.attribute.constvalue.AttributeType;
import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;

public class ProcessTaskStepFormAttributeVo {
	private Long processTaskId;
	private Long processTaskStepId;
	private Integer isEditable;
	private Integer isRequired;
	private String formUuid;
	private String processStepUuid;
	private String attributeUuid;
	private String config;
	private String data;
	private String label;
	private String help;
	private String unit;
	private String handler;
	private String handlerName;
	private String type;
	private String typeName;
	private String dataCubeTextField;
	private String dataCubeValueField;
	private String dataCubeUuid;
	private String inputPage;
	private String viewPage;
	private String configPage;
	private String description;
	private ProcessTaskAttributeVo attributeVo;
	private List<ProcessTaskAttributeValueVo> attributeValueList;
	private List<String> valueList;
	private ProcessTaskAttributeDataVo attributeData;
	private String editTemplate;
	private String viewTemplate;
	private String configTemplate;
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
		this.setUnit(processStepFormAttributeVo.getUnit());
		this.setHandler(processStepFormAttributeVo.getHandler());
		this.setType(processStepFormAttributeVo.getType());
		this.setDataCubeTextField(processStepFormAttributeVo.getDataCubeTextField());
		this.setDataCubeValueField(processStepFormAttributeVo.getDataCubeValueField());
		this.setDataCubeUuid(processStepFormAttributeVo.getDataCubeUuid());
		this.setInputPage(processStepFormAttributeVo.getInputPage());
		this.setViewPage(processStepFormAttributeVo.getViewPage());
		this.setConfigTemplate(processStepFormAttributeVo.getConfigTemplate());
		this.setEditTemplate(processStepFormAttributeVo.getEditTemplate());
		this.setViewTemplate(processStepFormAttributeVo.getViewTemplate());
	}

	public void addAttributeValue(ProcessTaskAttributeValueVo processTaskStepAttributeValueVo) {
		if (attributeValueList == null) {
			attributeValueList = new ArrayList<>();
		}
		attributeValueList.add(processTaskStepAttributeValueVo);
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

	public ProcessTaskAttributeVo getAttributeVo() {
		return attributeVo;
	}

	public void setAttributeVo(ProcessTaskAttributeVo attributeVo) {
		this.attributeVo = attributeVo;
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

	public void setViewPage(String viewPage) {
		this.viewPage = viewPage;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
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

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
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

	public void setConfigPage(String configPage) {
		this.configPage = configPage;
	}

	public String getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(String configTemplate) {
		this.configTemplate = configTemplate;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ProcessTaskAttributeValueVo> getAttributeValueList() {
		return attributeValueList;
	}

	public void setAttributeValueList(List<ProcessTaskAttributeValueVo> attributeValueList) {
		this.attributeValueList = attributeValueList;
	}

	public ProcessTaskAttributeDataVo getAttributeData() {
		return attributeData;
	}

	public void setAttributeData(ProcessTaskAttributeDataVo attributeData) {
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

	public List<String> getValueList() {
		return valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

}
