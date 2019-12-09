package codedriver.module.process.dto;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.constvalue.AttributeHandler;
import codedriver.framework.attribute.constvalue.AttributeType;

public class ProcessAttributeVo implements Serializable, Comparable<ProcessAttributeVo> {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 5046543035069620984L;
	private String attributeUuid;
	private String processUuid;
	private String name;
	private String label;
	private String type;
	private String typeName;
	private String handler;
	private String handlerName;
	private Integer isRequired;
	private String dataCubeUuid;
	private Integer width;
	private String unit;
	private String help;
	private String group;
	private String dataCubeValueField;
	private String dataCubeTextField;
	private Integer sort;
	private String config;
	private String description;
	private JSONObject configObj;


	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(Integer isRequired) {
		this.isRequired = isRequired;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getDataCubeValueField() {
		return dataCubeValueField;
	}

	public void setDataCubeValueField(String dataCubeValueField) {
		this.dataCubeValueField = dataCubeValueField;
	}

	public String getDataCubeTextField() {
		return dataCubeTextField;
	}

	public void setDataCubeTextField(String dataCubeTextField) {
		this.dataCubeTextField = dataCubeTextField;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
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

	@Override
	public int compareTo(ProcessAttributeVo o) {
		if (this.sort != null && o.getSort() != null) {
			return this.sort.compareTo(o.getSort());
		} else {
			return 0;
		}
	}

	public String getDataCubeUuid() {
		return dataCubeUuid;
	}

	public void setDataCubeUuid(String dataCubeUuid) {
		this.dataCubeUuid = dataCubeUuid;
	}

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getHandlerName() {
		if (StringUtils.isNotBlank(handler) && StringUtils.isBlank(handlerName)) {
			handlerName = AttributeHandler.getName(handler);
		}
		return handlerName;
	}

	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

}
