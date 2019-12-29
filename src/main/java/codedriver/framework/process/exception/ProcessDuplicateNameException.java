package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessDuplicateNameException extends ApiRuntimeException {

	private static final long serialVersionUID = -4617724920030245149L;

	public ProcessDuplicateNameException(String msg) {
		super("流程图名称：'" + msg + "'已存在");
	}
}
