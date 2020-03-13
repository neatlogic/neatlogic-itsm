package codedriver.framework.process.actionauthorityverificationhandler.handler;

import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.actionauthorityverificationhandler.core.ProcessTaskStepUserActionAuthorityVerificationHandlerBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
@Service
public class CompleteActionAuthorityVerificationHandler extends ProcessTaskStepUserActionAuthorityVerificationHandlerBase {

	@Override
	public String getAction() {
		return ProcessTaskStepAction.COMPLETE.getValue();
	}

	@Override
	protected boolean myTest(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
		// TODO linbq Auto-generated method stub
		UserContext.get().getUserId(true);
		return true;
	}

}
