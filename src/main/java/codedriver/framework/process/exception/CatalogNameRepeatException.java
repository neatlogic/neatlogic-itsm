package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class CatalogNameRepeatException extends ApiRuntimeException {

	private static final long serialVersionUID = -4617724920030245142L;

	public CatalogNameRepeatException(String msg) {
		super("服务目录名称：'" + msg + "'已存在");
	}
}
