package codedriver.module.process.stephandler.utilhandler;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerBase;
@Service
public class StartProcessUtilHandler extends ProcessStepUtilHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandler.START.getHandler();
	}

	@Override
	public Object getHandlerStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getHandlerStepInitInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject makeupConfig(JSONObject configObj) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    protected IOperationAuthHandlerType MyOperationAuthHandlerType() {
        return null;
    }

}
