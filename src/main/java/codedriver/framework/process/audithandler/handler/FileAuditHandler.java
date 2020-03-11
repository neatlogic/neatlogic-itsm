package codedriver.framework.process.audithandler.handler;

import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class FileAuditHandler implements IProcessTaskStepAuditDetailHandler{

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FILE.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		
	}

}
