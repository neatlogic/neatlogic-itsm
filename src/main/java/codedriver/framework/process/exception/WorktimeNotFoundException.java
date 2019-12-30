package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class WorktimeNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = 63938639408504619L;

	public WorktimeNotFoundException(String msg) {
		super("工作时间窗口:" + msg + "不存在");
	}
}
