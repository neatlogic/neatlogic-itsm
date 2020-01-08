package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormVersionNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = -3137999499740470766L;

	public FormVersionNotFoundException(String uuid) {
		super("表单版本：'" + uuid + "'不存在");
	}
}
