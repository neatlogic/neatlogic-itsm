package codedriver.module.process.dto;

import codedriver.module.process.constvalue.ProcessExpression;

public class ProcessExpressionVo {

	private String expression;
	private String expressionName;
	private String expressionEs;
	
	public ProcessExpressionVo() {
	}
	public ProcessExpressionVo(ProcessExpression processExpression) {
		this.expression = processExpression.getExpression();
		this.expressionName = processExpression.getExpressionName();
		this.expressionEs = processExpression.getExpressionEs();
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getExpressionName() {
		return expressionName;
	}
	public void setExpressionName(String expressionName) {
		this.expressionName = expressionName;
	}
	public String getExpressionEs() {
		return expressionEs;
	}
	public void setExpressionEs(String expressionEs) {
		this.expressionEs = expressionEs;
	}
	
}
