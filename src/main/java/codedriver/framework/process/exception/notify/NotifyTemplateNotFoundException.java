package codedriver.framework.process.exception.notify;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class NotifyTemplateNotFoundException extends ProcessTaskRuntimeException {
	private static final long serialVersionUID = 5036108230995602750L;

	public NotifyTemplateNotFoundException(String notifyTemplateUuid) {
		super("找不到uuid为：" + notifyTemplateUuid + "的通知模板");
	}
}
