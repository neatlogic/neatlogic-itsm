package codedriver.framework.process.exception;

public class ProcessTaskAbortException extends Exception {

	private static final long serialVersionUID = 5944378003653317742L;

	public ProcessTaskAbortException() {
		super();
	}

	public ProcessTaskAbortException(String msg) {
		super(msg);
	}

	public ProcessTaskAbortException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ProcessTaskAbortException(Throwable cause) {
		super(cause);
	}
}
