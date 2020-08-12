package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerBase;

public class OmnipotentProcessUtilHandler extends ProcessStepUtilHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandler.OMNIPOTENT.getHandler();
	}

	@Override
	public Object getHandlerStepInfo(Long processTaskStepId) {
		// TODO Auto-generated method stub
		return null;
	}

}
