package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessTaskStepNotFoundException extends ApiRuntimeException {
	
	private static final long serialVersionUID = -5334268232696017057L;

	public ProcessTaskStepNotFoundException(String processTaskStep) {
		super("流程步骤：'" + processTaskStep + "'不存在");
	}
}
