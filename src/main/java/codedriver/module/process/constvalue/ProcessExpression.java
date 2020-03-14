package codedriver.module.process.constvalue;

public enum ProcessExpression {
	LIKE("like", "包含", " %s match '%s' "),
	EQUAL("equal", "等于", " %s = '%s' "),
	UNEQUAL("unequal", "不等于", " not %s = '%s' "),
	INCLUDE("include", "包含", " %s contains any all ( %s ) "),
	EXCLUDE("exclude", "不包含", " not %s contains any all ( %s ) "),
	GREATERTHAN("greater-than", "大于", " %s > %s ) "),
	LESSTHAN("less-than", "小于", " %s < %s ) ");
	
	private String expression;
	private String expressionName;
	private String expressionEs;
	
	private ProcessExpression(String _expression, String _expressionName, String _expressionEs) {
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
		for (ProcessExpression s : ProcessExpression.values()) {
			if (s.getExpression().equals(_expression)) {
				return s.getExpressionName();
			}
		}
		return null;
	}
	
	public static String getExpressionEs(String _expression) {
		for (ProcessExpression s : ProcessExpression.values()) {
			if (s.getExpression().equals(_expression)) {
				return s.getExpressionEs();
			}
		}
		return null;
	}
}
