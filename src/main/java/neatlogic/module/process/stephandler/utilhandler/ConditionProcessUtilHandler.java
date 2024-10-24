package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dto.ProcessStepVo;
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
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		// TODO Auto-generated method stub

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

//	@Override
//	public JSONObject regulateProcessStepConfig(JSONObject configObj) {
//		if (configObj == null) {
//			configObj = new JSONObject();
//		}
//		JSONObject resultObj = new JSONObject();
//		List<MoveonConfigVo> moveonConfigList = new ArrayList<>();
//		JSONArray moveonConfigArray = configObj.getJSONArray("moveonConfigList");
//		if(CollectionUtils.isNotEmpty(moveonConfigArray)){
//			moveonConfigArray.removeIf(Objects::isNull);
//			List<String> effectiveStepUuidList = ProcessMessageManager.getEffectiveStepUuidList();
//			for(int i = 0; i < moveonConfigArray.size(); i++){
//				MoveonConfigVo moveonConfigVo = moveonConfigArray.getObject(i, MoveonConfigVo.class);
//				if(moveonConfigVo != null){
//					List<String> targetStepList = moveonConfigVo.getTargetStepList();
//					if (CollectionUtils.isNotEmpty(targetStepList)) {
//						List<String> list = ListUtils.removeAll(targetStepList, effectiveStepUuidList);
//						if (CollectionUtils.isNotEmpty(list)) {
//							throw new RuntimeException("条件步骤流转规则设置中存在无效的步骤");
//						}
//					}
////					targetStepList.removeIf(e -> !effectiveStepUuidList.contains(e));
////					if (CollectionUtils.isEmpty(targetStepList)) {
////						continue;
////					}
//					moveonConfigList.add(moveonConfigVo);
//				}
//			}
//		}
//		resultObj.put("moveonConfigList", moveonConfigList);
//		String formTag = configObj.getString("formTag");
//		if (StringUtils.isBlank(formTag)) {
//			formTag = StringUtils.EMPTY;
//		}
//		resultObj.put("formTag", formTag);
//		return resultObj;
//	}
}
