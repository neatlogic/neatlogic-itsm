package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class CatalogIllegalParameterException extends ApiRuntimeException {

	private static final long serialVersionUID = 183789473205326514L;

	public CatalogIllegalParameterException(String msg) {
		super(msg);
	}
}
