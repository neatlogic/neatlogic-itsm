package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormIllegalParameterException extends ApiRuntimeException {

	private static final long serialVersionUID = -500497012802362930L;

	public FormIllegalParameterException(String msg) {
		super(msg);
	}
}
