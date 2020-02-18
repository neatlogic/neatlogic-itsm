package codedriver.module.process.workcenter.dto;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class WorkcenterConditionVo implements Serializable{
	private static final long serialVersionUID = -776692828809703841L;
	
	private String uuid;
	private String name;
	private String displayName;
	private String type;
	private JSONObject config;
	private Integer sort;
	private String expression;
	private String[] valueList;
	
	
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
	
	public String[] getValueList() {
		return valueList;
	}

	public void setValueList(String[] valueList) {
		this.valueList = valueList;
	}



	public enum Type {
		TEXT("text"), SELECT("select"), MSELECT("mselect"), INPUTSELECT("inputselect"), TIMESCOPE("timescope"), TEXTAREA("textarea");
		private String name;

		private Type(String _name) {
			this.name = _name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public enum ProcessExpressionEs {
		EQUAL("equal", "等于", " %s like '%s'"),
		UNEQUAL("equal", "不等于", " not %s = '%s' "),
		INCLUDE("include", "包含", " %s contains any all ( %s )"),
		EXCLUDE("exclude", "不包含", " not %s contains any all ( %s )"),
		GREATERTHAN("greater-than", "大于", " %s > %s )"),
		LESSTHAN("less-than", "小于", " %s < %s )");
		
		private String expression;
		private String expressionName;
		private String expressionEs;
		
		private ProcessExpressionEs(String _expression, String _expressionName, String _expressionEs) {
			this.expression = _expression;
			this.expressionName = _expressionName;
			this.expressionEs = _expressionEs;
		}
		
		public String getExpression() {
			return expression;
		}
		
		public String getExpressionName() {
			return expressionName;
		}
		
		public String getExpressionEs() {
			return expressionEs;
		}

		
		public static String getExpressionName(String _expression) {
			for (ProcessExpressionEs s : ProcessExpressionEs.values()) {
				if (s.getExpression().equals(_expression)) {
					return s.getExpressionName();
				}
			}
			return null;
		}
		
		public static String getExpressionEs(String _expression) {
			for (ProcessExpressionEs s : ProcessExpressionEs.values()) {
				if (s.getExpression().equals(_expression)) {
					return s.getExpressionEs();
				}
			}
			return null;
		}
		
	}
}
