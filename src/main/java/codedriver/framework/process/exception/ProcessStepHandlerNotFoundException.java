package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessStepHandlerNotFoundException extends ApiRuntimeException {
	
	private static final long serialVersionUID = -5334268232696017057L;

	public ProcessStepHandlerNotFoundException(String handler) {
		super("找不到类型为：" + handler + "的流程组件");
	}
}
