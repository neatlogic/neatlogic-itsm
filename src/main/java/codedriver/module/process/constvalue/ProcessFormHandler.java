package codedriver.module.process.constvalue;

import java.util.Arrays;
import java.util.List;

public enum ProcessFormHandler {
	
	FORMSELECT("formselect","下拉框",ProcessFormHandlerType.SELECT,Arrays.asList(ProcessExpression.UNEQUAL,ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE),ProcessExpression.INCLUDE),
	FORMINPUT("forminput","文本框",ProcessFormHandlerType.INPUT,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LIKE),ProcessExpression.LIKE),
	FORMTEXTAREA("formtextarea","文本域",ProcessFormHandlerType.TEXTAREA,Arrays.asList(ProcessExpression.LIKE),ProcessExpression.LIKE),
	FORMEDITOR("formeditor","富文本框",ProcessFormHandlerType.EDITOR,Arrays.asList(ProcessExpression.LIKE),ProcessExpression.LIKE),
	FORMRADIO("formradio","单选框",ProcessFormHandlerType.RADIO,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL),ProcessExpression.EQUAL),
	FORMCHECKBOX("formcheckbox","复选框",ProcessFormHandlerType.CHECKBOX,Arrays.asList(ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE),ProcessExpression.INCLUDE),
	FORMDATE("formdate","日期",ProcessFormHandlerType.DATE,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LESSTHAN,ProcessExpression.GREATERTHAN),ProcessExpression.EQUAL),
	FORMTIME("formtime","时间",ProcessFormHandlerType.TIME,Arrays.asList(ProcessExpression.EQUAL,ProcessExpression.UNEQUAL,ProcessExpression.LESSTHAN,ProcessExpression.GREATERTHAN),ProcessExpression.EQUAL);
	
	private String handler;
	private String handlerName;
	private ProcessFormHandlerType type;
	private List<ProcessExpression> expressionList;
	private ProcessExpression expression;
	
	private ProcessFormHandler(String _handler,String _handlerName,ProcessFormHandlerType _type,List<ProcessExpression> _expressionList,ProcessExpression _expression) {
		this.handler = _handler;
		this.handlerName = _handlerName;
		this.type = _type;
		this.expressionList = _expressionList;
		this.expression = _expression;
	}
	
	
	public static String getHandlerName(String _handler) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getHandlerName();
			}
		}
		return null;
	}
	
	public static ProcessFormHandlerType getType(String _handler,String processWorkcenterConditionType) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				
				return s.getType(processWorkcenterConditionType);
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

	public ProcessFormHandlerType getType(String processWorkcenterConditionType) {
		if(ProcessWorkcenterConditionModel.CUSTOM.getValue().equals(processWorkcenterConditionType)) {
			if(type == ProcessFormHandlerType.RADIO || type == ProcessFormHandlerType.CHECKBOX) {
				return ProcessFormHandlerType.SELECT;
			}
			if(type == ProcessFormHandlerType.TEXTAREA || type == ProcessFormHandlerType.EDITOR) {
				return ProcessFormHandlerType.INPUT;
			}
		}
		return type;
	}

	public static ProcessExpression getExpression(String _handler) {
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getExpression();
			}
		}
		return null;
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


	public ProcessExpression getExpression() {
		return expression;
	}
	
	

}
