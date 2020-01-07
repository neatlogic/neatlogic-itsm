package codedriver.framework.process.exception.core;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessTaskRuntimeException extends ApiRuntimeException {
	private static final long serialVersionUID = 3270869999551703568L;

	public ProcessTaskRuntimeException() {
		super();
	}

	public ProcessTaskRuntimeException(String msg) {
		super(msg);
	}

	public ProcessTaskRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ProcessTaskRuntimeException(Throwable cause) {
		super(cause);
	}
}
