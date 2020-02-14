package codedriver.module.process.workcenter.dto;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class WorkcenterConditionVo implements Serializable{
	private static final long serialVersionUID = -776692828809703841L;
	
	private String name;
	private String displayName;
	private String type;
	private JSONObject config;
	private Integer sort;
	private String[] expressionList;
	
	
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


	public String[] getExpressionList() {
		return expressionList;
	}


	public void setExpressionList(String[] expressionList) {
		this.expressionList = expressionList;
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

	public enum ExpressionType {
		EQ("eq", "等于", "="), LIKE("like", "模糊查询", "like"), CONTAIN("contain", "模糊查询", "contain"),LT("lt", "小于", "&lt;"), GT("gt", "大于", "&gt;");
		private String name;
		private String text;
		private String expression;

		private ExpressionType(String _name, String _text, String _expression) {
			this.name = _name;
			this.text = _text;
			this.expression = _expression;
		}

		public String getValue() {
			return name;
		}

		public String getText() {
			return text;
		}

		public String getExpression() {
			return expression;
		}

		public static String getValue(String name) {
			for (ExpressionType s : ExpressionType.values()) {
				if (s.getValue().equals(name)) {
					return s.getExpression();
				}
			}
			return "";
		}

		public static String getText(String name) {
			for (ExpressionType s : ExpressionType.values()) {
				if (s.getValue().equals(name)) {
					return s.getText();
				}
			}
			return "";
		}
	}
}
