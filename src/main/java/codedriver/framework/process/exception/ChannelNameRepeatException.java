package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ChannelNameRepeatException extends ApiRuntimeException {

	private static final long serialVersionUID = -4617724920030245143L;

	public ChannelNameRepeatException(String msg) {
		super("服务通道名称：'" + msg + "'已存在");
	}
}
