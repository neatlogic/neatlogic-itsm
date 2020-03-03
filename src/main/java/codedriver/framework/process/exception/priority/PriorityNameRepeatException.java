package codedriver.framework.process.exception.priority;

import codedriver.framework.exception.core.ApiRuntimeException;

public class PriorityNameRepeatException extends ApiRuntimeException {

	private static final long serialVersionUID = -6084335002142350454L;

	public PriorityNameRepeatException(String name) {
		super("优先级名称：'" + name + "'已存在");
	}
}
