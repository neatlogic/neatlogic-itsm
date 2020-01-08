package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = -3973568374981840892L;

	public FormNotFoundException(String uuid) {
		super("表单：'" + uuid + "'不存在");
	}
}
