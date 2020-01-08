package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormNameRepeatException extends ApiRuntimeException {

	private static final long serialVersionUID = 1901910086387644808L;

	public FormNameRepeatException(String name) {
		super("表单名称：'" + name + "'已存在");
	}
}
