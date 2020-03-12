package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class WorkcenterConditionVo implements Serializable{
	private static final long serialVersionUID = -776692828809703841L;
	
	private String uuid;
	private String name;
	private String displayName;
	private String type;
	private String handler;
	private JSONObject config;
	private Integer sort;
	private String expression;
	private List<String> valueList;
	
	
	public WorkcenterConditionVo() {
		super();
	}
	
	public WorkcenterConditionVo(JSONObject jsonObj) {
		this.uuid = jsonObj.getString("uuid");
		this.name = jsonObj.getString("name").split("\\.")[1];
		this.type = jsonObj.getString("name").split("\\.")[0];
		this.expression = jsonObj.getString("expression");
		this.valueList = jsonObj.getJSONArray("valueList").toJavaList(String.class);
	}


	public String getUuid() {
		return uuid;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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

	public JSONObject getConfig() {
		return config;
	}


	public void setConfig(JSONObject config) {
		this.config = config;
	}


	public Integer getSort() {
		return sort;
	}


	public void setSort(Integer sort) {
		this.sort = sort;
	}


	public String getExpression() {
		return expression;
	}


	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public List<String> getValueList() {
		return valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	public enum Type {
		COMMON("common"), FORM("form");
		private String name;

		private Type(String _name) {
			this.name = _name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
