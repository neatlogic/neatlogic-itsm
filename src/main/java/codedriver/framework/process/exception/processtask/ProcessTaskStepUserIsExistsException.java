package codedriver.framework.process.exception.processtask;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class ProcessTaskStepUserIsExistsException extends ProcessTaskRuntimeException {
	private static final long serialVersionUID = 7077952789160608694L;

	public ProcessTaskStepUserIsExistsException(String userName) {
		super("处理人" + userName + "已存在");
	}
}
