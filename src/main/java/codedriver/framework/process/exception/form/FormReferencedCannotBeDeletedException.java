package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormReferencedCannotBeDeletedException extends ApiRuntimeException {

	private static final long serialVersionUID = 3459303366397256808L;

	public FormReferencedCannotBeDeletedException(String uuid) {
		super("表单：'" + uuid + "'有被引用，不能删除");
	};
}
