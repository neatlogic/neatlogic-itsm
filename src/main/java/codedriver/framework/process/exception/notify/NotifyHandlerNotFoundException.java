package codedriver.framework.process.exception.notify;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class NotifyHandlerNotFoundException extends ProcessTaskRuntimeException {
	private static final long serialVersionUID = 1775874801332152344L;

	public NotifyHandlerNotFoundException(String type) {
		super("找不到类型为：" + type + "的通知组件");
	}
}
