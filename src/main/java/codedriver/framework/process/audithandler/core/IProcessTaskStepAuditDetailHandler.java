package codedriver.framework.process.audithandler.core;

import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;

public interface IProcessTaskStepAuditDetailHandler {

	public String getType();
	
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo);
}
