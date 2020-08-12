package codedriver.module.process.stephandler.utilhandler;

import org.springframework.stereotype.Service;

import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerBase;
@Service
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

	@Override
	public Object getHandlerStepInitInfo(Long processTaskStepId) {
		// TODO Auto-generated method stub
		return null;
	}

}
