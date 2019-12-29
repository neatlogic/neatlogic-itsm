package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ChannelDuplicateNameException extends ApiRuntimeException {

	private static final long serialVersionUID = -4617724920030245143L;

	public ChannelDuplicateNameException(String msg) {
		super("服务通道名称：'" + msg + "'已存在");
	}
}
