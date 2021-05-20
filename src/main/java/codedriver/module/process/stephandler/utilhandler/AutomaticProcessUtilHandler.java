package codedriver.module.process.stephandler.utilhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.dto.processconfig.AutomaticCallbackConfigVo;
import codedriver.framework.process.dto.processconfig.AutomaticRequestConfigVo;
import codedriver.framework.process.dto.processconfig.AutomaticTimeWindowConfigVo;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.module.process.notify.handler.AutomaticNotifyPolicyHandler;
import codedriver.module.process.notify.handler.OmnipotentNotifyPolicyHandler;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;

@Service
public class AutomaticProcessUtilHandler extends ProcessStepInternalHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.AUTOMATIC.getHandler();
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
		/** 组装通知策略id **/
		JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
        Long policyId = notifyPolicyConfig.getLong("policyId");
        if(policyId != null) {
        	processStepVo.setNotifyPolicyId(policyId);
        }

		JSONArray actionList = (JSONArray) JSONPath.read(stepConfigObj.toJSONString(), "actionConfig.actionList");
		if(CollectionUtils.isNotEmpty(actionList)){
			for (int i = 0; i < actionList.size(); i++) {
				JSONObject ationObj = actionList.getJSONObject(i);
				String integrationUuid = ationObj.getString("integrationUuid");
				if(StringUtils.isNotBlank(integrationUuid)) {
					processStepVo.getIntegrationUuidList().add(integrationUuid);
				}
			}
		}

		/** 组装分配策略 **/
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isNotEmpty(workerPolicyConfig)) {
			JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
			if (CollectionUtils.isNotEmpty(policyList)) {
				List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
				for (int k = 0; k < policyList.size(); k++) {
					JSONObject policyObj = policyList.getJSONObject(k);
					if (!"1".equals(policyObj.getString("isChecked"))) {
						continue;
					}
					ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
					processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
					processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
					processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
					processStepWorkerPolicyVo.setSort(k + 1);
					processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
					workerPolicyList.add(processStepWorkerPolicyVo);
				}
				processStepVo.setWorkerPolicyList(workerPolicyList);
			}
		}
		/** 收集引用的外部调用uuid **/
		JSONObject automaticConfig = stepConfigObj.getJSONObject("automaticConfig");
		if(MapUtils.isNotEmpty(automaticConfig)) {
			JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
			if(MapUtils.isNotEmpty(requestConfig)) {
				String integrationUuid = requestConfig.getString("integrationUuid");
				if(StringUtils.isNotBlank(integrationUuid)) {
					processStepVo.getIntegrationUuidList().add(integrationUuid);
				}
			}
			JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
			if(MapUtils.isNotEmpty(callbackConfig)) {
				JSONObject config = callbackConfig.getJSONObject("config");
				if(MapUtils.isNotEmpty(config)) {
					String integrationUuid = config.getString("integrationUuid");
					if(StringUtils.isNotBlank(integrationUuid)) {
						processStepVo.getIntegrationUuidList().add(integrationUuid);
					}
				}
			}
		}
	}
	
	@Override
	public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
	}
	
	@SuppressWarnings("serial")
	@Override
	public JSONObject makeupConfig(JSONObject configObj) {
		if(configObj == null) {
			configObj = new JSONObject();
		}
		JSONObject resultObj = new JSONObject();
		
		/** 授权 **/
		JSONArray authorityArray = new JSONArray();
		ProcessTaskOperationType[] stepActions = {
				ProcessTaskOperationType.STEP_VIEW, 
				ProcessTaskOperationType.STEP_TRANSFER
		};
		for(ProcessTaskOperationType stepAction : stepActions) {
			authorityArray.add(new JSONObject() {{
				this.put("action", stepAction.getValue());
				this.put("text", stepAction.getText());
				this.put("acceptList", stepAction.getAcceptList());
				this.put("groupList", stepAction.getGroupList());
			}});
		}
		JSONArray authorityList = configObj.getJSONArray("authorityList");
		if(CollectionUtils.isNotEmpty(authorityList)) {
			Map<String, JSONArray> authorityMap = new HashMap<>();
			for(int i = 0; i < authorityList.size(); i++) {
				JSONObject authority = authorityList.getJSONObject(i);
				authorityMap.put(authority.getString("action"), authority.getJSONArray("acceptList"));
			}
			for(int i = 0; i < authorityArray.size(); i++) {
				JSONObject authority = authorityArray.getJSONObject(i);
				JSONArray acceptList = authorityMap.get(authority.getString("action"));
				if(acceptList != null) {
					authority.put("acceptList", acceptList);
				}
			}
		}
		resultObj.put("authorityList", authorityArray);
		
		/** 按钮映射 **/
		JSONArray customButtonArray = new JSONArray();
		ProcessTaskOperationType[] stepButtons = {
				ProcessTaskOperationType.STEP_COMPLETE, 
				ProcessTaskOperationType.STEP_BACK, 
				ProcessTaskOperationType.TASK_TRANSFER, 
				ProcessTaskOperationType.STEP_START
		};
		for(ProcessTaskOperationType stepButton : stepButtons) {
			customButtonArray.add(new JSONObject() {{
				this.put("name", stepButton.getValue());
				this.put("customText", stepButton.getText());
				this.put("value", "");
			}});
		}

		JSONArray customButtonList = configObj.getJSONArray("customButtonList");
		if(CollectionUtils.isNotEmpty(customButtonList)) {
			Map<String, String> customButtonMap = new HashMap<>();
			for(int i = 0; i < customButtonList.size(); i++) {
				JSONObject customButton = customButtonList.getJSONObject(i);
				customButtonMap.put(customButton.getString("name"), customButton.getString("value"));
			}
			for(int i = 0; i < customButtonArray.size(); i++) {
				JSONObject customButton = customButtonArray.getJSONObject(i);
				String value = customButtonMap.get(customButton.getString("name"));
				if(StringUtils.isNotBlank(value)) {
					customButton.put("value", value);
				}
			}
		}
		resultObj.put("customButtonList", customButtonArray);
		
		/** 通知 **/
		JSONObject notifyPolicyObj = new JSONObject();
		JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
		if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
			notifyPolicyObj.putAll(notifyPolicyConfig);
		}
		notifyPolicyObj.put("handler", AutomaticNotifyPolicyHandler.class.getName());
		resultObj.put("notifyPolicyConfig", notifyPolicyObj);

		return resultObj;
	}

	@Override
	public JSONObject makeupProcessStepConfig(JSONObject configObj) {
		if (configObj == null) {
			configObj = new JSONObject();
		}
		JSONObject resultObj = new JSONObject();

		/** 授权 **/
		ProcessTaskOperationType[] stepActions = {
				ProcessTaskOperationType.STEP_VIEW,
				ProcessTaskOperationType.STEP_TRANSFER
		};
		JSONArray authorityList = configObj.getJSONArray("authorityList");
		if (CollectionUtils.isNotEmpty(authorityList)) {
			JSONArray authorityArray = ProcessConfigUtil.makeupAuthorityList(authorityList, stepActions);
			resultObj.put("authorityList", authorityArray);
		}

		/** 通知 **/
		JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
		if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
			JSONObject notifyPolicyObj = ProcessConfigUtil.makeupNotifyPolicyConfig(notifyPolicyConfig, OmnipotentNotifyPolicyHandler.class);
			resultObj.put("notifyPolicyConfig", notifyPolicyObj);
		}

		JSONArray customButtonList = configObj.getJSONArray("customButtonList");
		if (CollectionUtils.isNotEmpty(customButtonList)) {
			/** 按钮映射列表 **/
			ProcessTaskOperationType[] stepButtons = {
					ProcessTaskOperationType.STEP_COMPLETE,
					ProcessTaskOperationType.STEP_BACK,
					ProcessTaskOperationType.TASK_TRANSFER,
					ProcessTaskOperationType.STEP_START
			};
			JSONArray customButtonArray = ProcessConfigUtil.makeupCustomButtonList(customButtonList, stepButtons);
			resultObj.put("customButtonList", customButtonArray);
		}
		/** 状态映射列表 **/
		JSONArray customStatusList = configObj.getJSONArray("customStatusList");
		if (CollectionUtils.isNotEmpty(customStatusList)) {
			JSONArray customStatusArray = ProcessConfigUtil.makeupCustomStatusList(customStatusList);
			resultObj.put("customStatusList", customStatusArray);
		}

		/** 自动化配置 **/
//		String failPolicy = "";
//		String integrationUuid = "";
//		JSONArray paramArray = new JSONArray();
//		JSONObject successObj = new JSONObject();
//		String resultTemplate = "";
//		JSONObject requestObj = new JSONObject();

//		String type = "none";
//		JSONObject intervalObj = new JSONObject();
//		JSONObject timeWindowObj = new JSONObject();

		AutomaticRequestConfigVo requestConfigVo = null;
		AutomaticCallbackConfigVo callbackConfigVo = null;
		AutomaticTimeWindowConfigVo timeWindowConfigVo = null;
		JSONObject automaticConfig = configObj.getJSONObject("automaticConfig");
		if (MapUtils.isNotEmpty(automaticConfig)) {
			/** 外部调用 **/
			JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
			if (MapUtils.isNotEmpty(requestConfig)) {
				requestConfigVo = JSONObject.toJavaObject(requestConfig, AutomaticRequestConfigVo.class);
//				failPolicy = requestConfig.getString("failPolicy");
//				integrationUuid = requestConfig.getString("integrationUuid");
//				JSONArray paramList = requestConfig.getJSONArray("paramList");
//				if (CollectionUtils.isNotEmpty(paramList)) {
//					for (int i = 0; i < paramList.size(); i++) {
//						JSONObject paramConfig = paramList.getJSONObject(i);
//						if (MapUtils.isNotEmpty(paramConfig)) {
//							String name = paramConfig.getString("name");
//							String mappingType = paramConfig.getString("type");
//							String value = paramConfig.getString("value");
//							JSONObject paramObj = new JSONObject();
//							paramObj.put("name", name);
//							paramObj.put("type", mappingType);
//							paramObj.put("value", value);
//							paramArray.add(paramObj);
//						}
//					}
//				}
//				JSONObject successConfig = requestConfig.getJSONObject("successConfig");
//				if (MapUtils.isNotEmpty(successConfig)) {
//					String name = successConfig.getString("name");
//					String expression = successConfig.getString("expression");
//					String value = successConfig.getString("value");
//					successObj.put("name", name);
//					successObj.put("expression", expression);
//					successObj.put("value", value);
//				}
//				resultTemplate = requestConfig.getString("resultTemplate");
			}
//			requestObj.put("failPolicy", failPolicy);
//			requestObj.put("integrationUuid", integrationUuid);
//			requestObj.put("paramList", paramArray);
//			requestObj.put("successConfig", successObj);
//			requestObj.put("resultTemplate", resultTemplate);
			/** 是否回调 **/
			JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
			if (MapUtils.isNotEmpty(callbackConfig)) {
				callbackConfigVo = JSONObject.toJavaObject(callbackConfig, AutomaticCallbackConfigVo.class);
//				type = callbackConfig.getString("type");
//				JSONObject intervalConfig = callbackConfig.getJSONObject("config");
//				if (MapUtils.isNotEmpty(intervalConfig)) {
//					String intervalIntegrationUuid = intervalConfig.getString("integrationUuid");
//					Integer interval = intervalConfig.getInteger("interval");
//					String intervalResultTemplate = intervalConfig.getString("resultTemplate");
//					intervalObj.put("integrationUuid", intervalIntegrationUuid);
//					intervalObj.put("interval", interval);
//					intervalObj.put("resultTemplate", intervalResultTemplate);
//				}
			}
			/** 时间窗口 **/
			JSONObject timeWindowConfig = automaticConfig.getJSONObject("timeWindowConfig");
			if (MapUtils.isNotEmpty(timeWindowConfig)) {
				timeWindowConfigVo = JSONObject.toJavaObject(timeWindowConfig, AutomaticTimeWindowConfigVo.class);
//				String startTime = timeWindowConfig.getString("startTime");
//				String endTime = timeWindowConfig.getString("endTime");
//				timeWindowObj.put("startTime", startTime);
//				timeWindowObj.put("endTime", endTime);
			}
		}

		JSONObject automaticObj = new JSONObject();
//		automaticObj.put("requestConfig", requestObj);

//		JSONObject callbackObj = new JSONObject();
//		callbackObj.put("type", type);
//		callbackObj.put("config", intervalObj);
//		automaticObj.put("callbackConfig", callbackObj);

//		automaticObj.put("timeWindowConfig", timeWindowObj);

		automaticObj.put("requestConfig", requestConfigVo);
		automaticObj.put("callbackConfig", callbackConfigVo);
		automaticObj.put("timeWindowConfig", timeWindowConfigVo);
		resultObj.put("automaticConfig", automaticObj);
		/** 分配处理人 **/
		JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isNotEmpty(workerPolicyConfig)) {
			JSONObject workerPolicyObj = ProcessConfigUtil.makeupWorkerPolicyConfig(workerPolicyConfig);
			resultObj.put("workerPolicyConfig", workerPolicyObj);
		}
		return resultObj;
	}

}
