package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerBase;

public class AutomaticProcessUtilHandler extends ProcessStepUtilHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandler.AUTOMATIC.getHandler();
	}

	@Override
	public Object getHandlerStepInfo(Long processTaskStepId) {
		// TODO Auto-generated method stub
		return null;
	}

}
