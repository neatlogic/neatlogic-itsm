package codedriver.framework.process.exception.process;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class ProcessNameRepeatException extends ProcessTaskRuntimeException {

	private static final long serialVersionUID = -4617724920030245149L;

	public ProcessNameRepeatException(String msg) {
		super("流程图名称：'" + msg + "'已存在");
	}
}
