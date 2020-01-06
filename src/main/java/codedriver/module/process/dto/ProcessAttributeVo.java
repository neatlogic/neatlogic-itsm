package codedriver.module.process.dto;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.constvalue.AttributeHandler;
import codedriver.framework.attribute.constvalue.AttributeType;

public class ProcessAttributeVo implements Serializable{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 5046543035069620984L;
	private String attributeUuid;
	private String processUuid;
	private String label;
	private String type;
	private String typeName;
	private String handler;
	private String handlerName;
	private String config;
	private JSONObject configObj;

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

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
