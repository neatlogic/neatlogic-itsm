package codedriver.framework.process.exception.form;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FormActiveVersionCannotBeDeletedException extends ApiRuntimeException {

	private static final long serialVersionUID = -8664693189378478063L;

	public FormActiveVersionCannotBeDeletedException(String uuid) {
		super("表单版本：" + uuid + "为当前激活版本，不能删除");
	}
}
