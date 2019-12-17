package codedriver.framework.process.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ChannelNotFoundException extends ApiRuntimeException {
	
	private static final long serialVersionUID = -5334268232696017057L;

	public ChannelNotFoundException(String channelUuid) {
		super("服务通道：'" + channelUuid + "'不存在");
	}
}
