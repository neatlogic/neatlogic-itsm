package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class WorktimeDefineIsEmptyException extends ApiRuntimeException {

	private static final long serialVersionUID = -3155074946981978438L;

	public WorktimeDefineIsEmptyException(String msg) {
		super("工作时间窗口：'" + msg + "'的工作时段未定义");
	}
}
