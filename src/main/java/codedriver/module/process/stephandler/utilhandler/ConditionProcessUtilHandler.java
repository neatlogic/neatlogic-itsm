package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.process.dto.processconfig.MoveonConfigVo;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ConditionProcessUtilHandler extends ProcessStepInternalHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.CONDITION.getHandler();
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
	public JSONObject regulateProcessStepConfig(JSONObject configObj) {
		if (configObj == null) {
			configObj = new JSONObject();
		}
		JSONObject resultObj = new JSONObject();
		List<MoveonConfigVo> moveonConfigList = new ArrayList<>();
		JSONArray moveonConfigArray = configObj.getJSONArray("moveonConfigList");
		if(CollectionUtils.isNotEmpty(moveonConfigArray)){
			moveonConfigArray.removeIf(Objects::isNull);
			for(int i = 0; i < moveonConfigArray.size(); i++){
				MoveonConfigVo moveonConfigVo = moveonConfigArray.getObject(i, MoveonConfigVo.class);
				if(moveonConfigVo != null){
					moveonConfigList.add(moveonConfigVo);
				}
			}
		}
		resultObj.put("moveonConfigList", moveonConfigList);
		return resultObj;
	}
}
