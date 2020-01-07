package codedriver.framework.process.exception.core;

import codedriver.framework.exception.core.ApiException;

public class ProcessTaskException extends ApiException {

	private static final long serialVersionUID = 4314481891500443152L;

	public ProcessTaskException() {
		super();
	}

	public ProcessTaskException(String msg) {
		super(msg);
	}

	public ProcessTaskException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ProcessTaskException(Throwable cause) {
		super(cause);
	}
}
