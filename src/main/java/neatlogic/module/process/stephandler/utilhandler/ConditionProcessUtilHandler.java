package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import org.springframework.stereotype.Service;

@Service
public class ConditionProcessUtilHandler extends ProcessStepInternalHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.CONDITION.getHandler();
	}

	@Override
	public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject makeupConfig(JSONObject configObj) {
		if (configObj == null) {
			configObj = new JSONObject();
		}
		return configObj;
	}

	@Override
	public String[] getRegulateKeyList() {
		return new String[]{"moveonConfigList", "formTag"};
	}

}
