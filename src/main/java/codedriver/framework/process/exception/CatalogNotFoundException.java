package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class CatalogNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = 4478080139019340481L;

	public CatalogNotFoundException(String uuid) {
		super("服务目录：'" + uuid + "'不存在");
	}
}
