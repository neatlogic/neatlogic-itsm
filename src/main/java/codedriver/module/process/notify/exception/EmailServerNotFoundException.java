package codedriver.module.process.notify.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-12 10:20
 **/
public class EmailServerNotFoundException extends ApiRuntimeException {
	private static final long serialVersionUID = 3293831889717207715L;

	public EmailServerNotFoundException() {
		super("没有激活的邮件服务器");
	}
}
