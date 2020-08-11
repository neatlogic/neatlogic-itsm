package codedriver.module.process.audithandler.handler;

import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class ContentAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.CONTENT.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {		
		return 1;
	}

}
