package codedriver.module.process.stephandler.utilhandler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.handler.TaskNotifyPolicyHandler;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerBase;
@Service
public class EndProcessUtilHandler extends ProcessStepUtilHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.END.getHandler();
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
              ProcessTaskOperationType.ABORTPROCESSTASK, 
              ProcessTaskOperationType.UPDATE, 
              ProcessTaskOperationType.URGE, 
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
        
        /** 按钮映射列表 **/
//        JSONArray customButtonArray = new JSONArray();
//        ProcessTaskOperationType[] stepButtons = {
//                ProcessTaskOperationType.ABORTPROCESSTASK, 
//                ProcessTaskOperationType.RECOVERPROCESSTASK
//        };
//        for(ProcessTaskOperationType stepButton : stepButtons) {
//            customButtonArray.add(new JSONObject() {{
//                this.put("name", stepButton.getValue());
//                this.put("customText", stepButton.getText());
//                this.put("value", "");
//            }});
//        }
//        
//        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
//        if(CollectionUtils.isNotEmpty(customButtonList)) {
//            Map<String, String> customButtonMap = new HashMap<>();
//            for(int i = 0; i < customButtonList.size(); i++) {
//                JSONObject customButton = customButtonList.getJSONObject(i);
//                customButtonMap.put(customButton.getString("name"), customButton.getString("value"));
//            }
//            for(int i = 0; i < customButtonArray.size(); i++) {
//                JSONObject customButton = customButtonArray.getJSONObject(i);
//                String value = customButtonMap.get(customButton.getString("name"));
//                if(StringUtils.isNotBlank(value)) {
//                    customButton.put("value", value);
//                }
//            }
//        }
//        resultObj.put("customButtonList", customButtonArray);
        
        /** 通知 **/
        JSONObject notifyPolicyObj = new JSONObject();
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
            notifyPolicyObj.putAll(notifyPolicyConfig);
        }
        notifyPolicyObj.put("handler", TaskNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyObj);
        
        return resultObj;
	}

    @Override
    protected IOperationAuthHandlerType MyOperationAuthHandlerType() {
        return null;
    }

}
