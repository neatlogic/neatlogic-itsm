package codedriver.framework.process.exception.process;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class ProcessStepHandlerNotFoundException extends ProcessTaskRuntimeException {
	
	private static final long serialVersionUID = -5334268232696017057L;

	public ProcessStepHandlerNotFoundException(String handler) {
		super("找不到类型为：" + handler + "的流程组件");
	}
}
