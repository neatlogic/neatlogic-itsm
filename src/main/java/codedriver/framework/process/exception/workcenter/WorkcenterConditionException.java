package codedriver.framework.process.exception.workcenter;

import codedriver.framework.exception.core.ApiRuntimeException;

public class WorkcenterConditionException extends ApiRuntimeException {

	private static final long serialVersionUID = 1901910086387644808L;

	public WorkcenterConditionException(String name) {
		super("工单中心过滤条件 参数：'" + name + "'非法");
	}
}
