package codedriver.framework.process.exception.processtask;

import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;

public class ProcessTaskNoPermissionException extends ProcessTaskRuntimeException  {

	private static final long serialVersionUID = -7145916738483615561L;

	public ProcessTaskNoPermissionException(String action){
		super("您没有执行'" + action + "'操作权限");
	}
	
}
