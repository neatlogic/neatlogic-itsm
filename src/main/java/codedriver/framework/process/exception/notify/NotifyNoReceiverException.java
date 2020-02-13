package codedriver.framework.process.exception.notify;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class NotifyNoReceiverException extends ProcessTaskRuntimeException {
	private static final long serialVersionUID = 1775874801332152344L;

	public NotifyNoReceiverException() {
		super("没有收件人");
	}
}
