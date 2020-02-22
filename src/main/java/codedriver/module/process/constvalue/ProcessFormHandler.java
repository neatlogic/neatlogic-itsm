package codedriver.module.process.constvalue;

import java.util.Arrays;
import java.util.List;

public enum ProcessFormHandler {
	
	FORMSELECT("formselect","下拉框",ProcessFormHandlerType.SELECT,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE)),
	FORMINPUT("forminput","文本框",ProcessFormHandlerType.INPUT,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LIKE)),
	FORMAREA("formarea","文本域",ProcessFormHandlerType.TEXTAREA,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LIKE)),
	FORMRADIO("formradio","单选框",ProcessFormHandlerType.RADIO,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL)),
	FORMCHECKBOX("formcheckbox","复选框",ProcessFormHandlerType.CHECKBOX,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE)),
	FORMDATE("formdate","日期",ProcessFormHandlerType.DATE,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LESSTHAN,ProcessExpression.GREATERTHAN)),
	FORMTime("formtime","时间",ProcessFormHandlerType.TIME,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LESSTHAN,ProcessExpression.GREATERTHAN));
	
	private String handler;
	private String handlerName;
	private ProcessFormHandlerType type;
	private List<ProcessExpression> expressionList;
	
	private ProcessFormHandler(String _handler,String _handlerName,ProcessFormHandlerType _type,List<ProcessExpression> _expressionList) {
		this.handler = _handler;
		this.handlerName = _handlerName;
		this.type = _type;
		this.expressionList = _expressionList;
	}
	
	
	public static String getHandlerName(String _handler) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getHandlerName();
			}
		}
		return null;
	}
	
	public static ProcessFormHandlerType getType(String _handler) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getType();
			}
		}
		return null;
	}
	
	public static List<ProcessExpression> getExpressionList(String _handler) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getExpressionList();
			}
		}
		return null;
	}

	public ProcessFormHandlerType getType() {
		return type;
	}

	public List<ProcessExpression> getExpressionList() {
		return expressionList;
	}

	public String getHandler() {
		return handler;
	}


	public String getHandlerName() {
		return handlerName;
	}
	
	

}
