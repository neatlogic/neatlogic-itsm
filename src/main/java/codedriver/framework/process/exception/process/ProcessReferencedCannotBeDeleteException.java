package codedriver.framework.process.exception.process;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ProcessReferencedCannotBeDeleteException extends ApiRuntimeException {

	private static final long serialVersionUID = -6726808807183027552L;

	public ProcessReferencedCannotBeDeleteException(String uuid) {
		super("流程：'" + uuid + "'又被引用，不能删除");
	}
}
