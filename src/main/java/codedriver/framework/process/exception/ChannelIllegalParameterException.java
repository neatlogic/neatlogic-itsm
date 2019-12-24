package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ChannelIllegalParameterException extends ApiRuntimeException {

	private static final long serialVersionUID = 1747171456984633383L;

	public ChannelIllegalParameterException(String msg) {
		super(msg);
	}
}
