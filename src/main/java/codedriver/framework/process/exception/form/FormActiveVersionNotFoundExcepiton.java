package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormActiveVersionNotFoundExcepiton extends ApiRuntimeException {

	private static final long serialVersionUID = 6933170692575758579L;

	public FormActiveVersionNotFoundExcepiton(String uuid) {
		super("表单：'" + uuid + "'没有激活版本");
	}
}
