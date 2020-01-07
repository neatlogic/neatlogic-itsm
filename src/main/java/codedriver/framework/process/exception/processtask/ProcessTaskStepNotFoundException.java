package codedriver.framework.process.exception.processtask;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class ProcessTaskStepNotFoundException extends ProcessTaskRuntimeException {

	private static final long serialVersionUID = 186121675355812447L;

	public ProcessTaskStepNotFoundException(String processTaskStep) {
		super("流程步骤：'" + processTaskStep + "'不存在");
	}
}
