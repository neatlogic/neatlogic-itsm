package codedriver.framework.process.exception.channel;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ChannelUnsupportedOperationException extends ApiRuntimeException {

	private static final long serialVersionUID = -1512919723351654849L;

	public ChannelUnsupportedOperationException(String uuid, String msg) {
		super("服务通道：'" + uuid +"'不支持'" + msg + "'操作");
	}
}
