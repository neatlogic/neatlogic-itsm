package codedriver.framework.process.exception.process;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessDraftNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = -7136586762433623298L;
	
	public ProcessDraftNotFoundException(String uuid) {
		super("流程草稿：'" + uuid + "'不存在");
	}
}
