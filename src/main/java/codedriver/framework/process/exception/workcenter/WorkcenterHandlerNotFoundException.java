package codedriver.framework.process.exception.workcenter;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class WorkcenterHandlerNotFoundException extends ProcessTaskRuntimeException {

	private static final long serialVersionUID = 8358695524151979636L;

	public WorkcenterHandlerNotFoundException(String handler) {
		super("找不到类型为：" + handler + "的工单中心处理器");
	}
}
