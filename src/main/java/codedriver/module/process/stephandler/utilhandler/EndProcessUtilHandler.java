package codedriver.module.process.stephandler.utilhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.module.process.notify.handler.OmnipotentNotifyPolicyHandler;
import codedriver.module.process.notify.handler.SlaNotifyPolicyHandler;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.notify.handler.TaskNotifyPolicyHandler;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;

@Service
public class EndProcessUtilHandler extends ProcessStepInternalHandlerBase {

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
              ProcessTaskOperationType.TASK_ABORT, 
              ProcessTaskOperationType.TASK_UPDATE, 
              ProcessTaskOperationType.TASK_URGE
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
        
        /** 通知 **/
        JSONObject notifyPolicyObj = new JSONObject();
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
            notifyPolicyObj.putAll(notifyPolicyConfig);
        }
        notifyPolicyObj.put("handler", TaskNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyObj);
        
        /** 动作 **/
        JSONObject actionConfig = configObj.getJSONObject("actionConfig");
        if(actionConfig == null) {
            actionConfig = new JSONObject();
        }
        actionConfig.put("handler", TaskNotifyPolicyHandler.class.getName());
        actionConfig.put("integrationHandler", "");
        resultObj.put("actionConfig", actionConfig);
        return resultObj;
	}

    @Override
    public JSONObject makeupProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();
        /** 流程设置 **/
        String uuid = "";
        String name = "";
        JSONArray authorityArray = new JSONArray();
        JSONObject notifyPolicyObj = new JSONObject();
        JSONObject actionObj = new JSONObject();
        JSONObject processConfig = configObj.getJSONObject("processConfig");
        if (MapUtils.isNotEmpty(processConfig)) {
            uuid = processConfig.getString("uuid");
            name = processConfig.getString("name");
            /** 授权 **/
            ProcessTaskOperationType[] stepActions = {
                    ProcessTaskOperationType.TASK_ABORT,
                    ProcessTaskOperationType.TASK_UPDATE,
                    ProcessTaskOperationType.TASK_URGE
            };
            JSONArray authorityList = processConfig.getJSONArray("authorityList");
            if (CollectionUtils.isNotEmpty(authorityList)) {
                authorityArray = ProcessConfigUtil.makeupAuthorityList(authorityList, stepActions);
            }

            /** 通知 **/
            JSONObject notifyPolicyConfig = processConfig.getJSONObject("notifyPolicyConfig");
            if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                notifyPolicyObj = ProcessConfigUtil.makeupNotifyPolicyConfig(notifyPolicyConfig, TaskNotifyPolicyHandler.class);
            }

            /** 动作 **/
            JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
            if (MapUtils.isNotEmpty(actionConfig)) {
                actionObj = ProcessConfigUtil.makeupActionConfig(actionConfig, TaskNotifyPolicyHandler.class);
            }
        }
        JSONObject processObj = new JSONObject();
        processObj.put("uuid", uuid);
        processObj.put("name", name);
        processObj.put("authorityList", authorityArray);
        processObj.put("notifyPolicyConfig", notifyPolicyObj);
        processObj.put("actionConfig", actionObj);
        resultObj.put("processConfig", processObj);
        /** 表单设置 **/
        String formUuid = "";
        JSONArray formAuthorityArray = new JSONArray();
        JSONObject formConfig = configObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfig)) {
            formUuid = formConfig.getString("uuid");
            JSONArray authorityList = formConfig.getJSONArray("authorityList");
            for (int i = 0; i < authorityList.size(); i++) {
                JSONObject authority = authorityList.getJSONObject(i);
                if (MapUtils.isNotEmpty(authority)) {
                    JSONArray attributeUuidList = authority.getJSONArray("attributeUuidList");
                    JSONArray processStepUuidList = authority.getJSONArray("processStepUuidList");
                    String action = authority.getString("action");
                    String type = authority.getString("type");
                    JSONObject authorityObj = new JSONObject();
                    authorityObj.put("attributeUuidList", attributeUuidList);
                    authorityObj.put("processStepUuidList", processStepUuidList);
                    authorityObj.put("action", action);
                    authorityObj.put("type", type);
                    formAuthorityArray.add(authorityObj);
                }
            }
        }
        JSONObject formObj = new JSONObject();
        formObj.put("uuid", formUuid);
        formObj.put("authorityList", formAuthorityArray);
        resultObj.put("formConfig", formObj);
        /** 评分设置 **/
        JSONObject scoreConfigObj = new JSONObject();
        Integer isActive = 0;
        JSONObject scoreConfig = configObj.getJSONObject("scoreConfig");
        if (MapUtils.isNotEmpty(scoreConfig)) {
            isActive = scoreConfig.getInteger("isActive");
            if (Objects.equals(isActive, 1)) {
                Long scoreTemplateId = scoreConfig.getLong("scoreTemplateId");
                Integer isAuto = scoreConfig.getInteger("isAuto");
                if (Objects.equals(isAuto, 1)) {
                    String autoTimeType = "naturalDay";
                    Integer autoTime = 3;
                    JSONObject autoConfig = scoreConfig.getJSONObject("config");
                    if (MapUtils.isNotEmpty(autoConfig)) {
                        autoTimeType = autoConfig.getString("autoTimeType");
                        autoTime = autoConfig.getInteger("autoTime");
                    }
                    JSONObject autoConfigObj = new JSONObject();
                    autoConfigObj.put("autoTimeType", autoTimeType);
                    autoConfigObj.put("autoTime", autoTime);
                    scoreConfigObj.put("config", autoConfigObj);
                } else {
                    isAuto = 0;
                }
                scoreConfigObj.put("scoreTemplateId", scoreTemplateId);
                scoreConfigObj.put("isAuto", isAuto);
            }
        }
        scoreConfigObj.put("isActive", isActive);
        resultObj.put("scoreConfig", scoreConfigObj);
        /** 时效设置 **/
        JSONArray slaArray = new JSONArray();
        JSONArray slaList = configObj.getJSONArray("slaList");
        if (CollectionUtils.isNotEmpty(slaList)) {
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject sla = slaList.getJSONObject(i);
                if (MapUtils.isNotEmpty(sla)) {
                    JSONObject slaObj = new JSONObject();
                    JSONArray transferPolicyArray = new JSONArray();
                    JSONArray transferPolicyList = sla.getJSONArray("transferPolicyList");
                    if (CollectionUtils.isNotEmpty(transferPolicyList)) {
                        for (int j = 0; j < transferPolicyList.size(); j++) {
                            JSONObject transferPolicy = transferPolicyList.getJSONObject(j);
                            if (MapUtils.isNotEmpty(transferPolicy)) {
                                String unit = transferPolicy.getString("unit");
                                String expression = transferPolicy.getString("expression");
                                String transferTo = transferPolicy.getString("transferTo");
                                String transferPolicyUuid = transferPolicy.getString("uuid");
                                JSONObject transferPolicyObj = new JSONObject();
                                transferPolicyObj.put("unit", unit);
                                transferPolicyObj.put("expression", expression);
                                transferPolicyObj.put("transferTo", transferTo);
                                transferPolicyObj.put("uuid", transferPolicyUuid);
                                transferPolicyArray.add(transferPolicyObj);
                            }
                        }
                    }
                    slaObj.put("transferPolicyList", transferPolicyArray);

                    JSONArray processStepUuidList = sla.getJSONArray("processStepUuidList");
                    if (processStepUuidList == null) {
                        processStepUuidList = new JSONArray();
                    }
                    slaObj.put("processStepUuidList", processStepUuidList);
                    JSONArray calculatePolicyArray = new JSONArray();
                    JSONArray calculatePolicyList = sla.getJSONArray("calculatePolicyList");
                    if (CollectionUtils.isNotEmpty(calculatePolicyList)) {
                        for (int j = 0; j < calculatePolicyList.size(); j++) {
                            JSONObject calculatePolicy = calculatePolicyList.getJSONObject(j);
                            if (MapUtils.isNotEmpty(calculatePolicy)) {
                                Integer enablePriority = calculatePolicy.getInteger("enablePriority");
                                Boolean isshow = calculatePolicy.getBoolean("isshow");
                                String unit = calculatePolicy.getString("unit");
                                String calculatePolicyUuid = calculatePolicy.getString("uuid");
                                JSONArray priorityList = calculatePolicy.getJSONArray("priorityList");
                                JSONArray conditionGroupList = calculatePolicy.getJSONArray("conditionGroupList");
                                JSONArray conditionGroupRelList = calculatePolicy.getJSONArray("conditionGroupRelList");
                                JSONObject calculatePolicyObj = new JSONObject();
                                calculatePolicyObj.put("enablePriority", enablePriority);
                                calculatePolicyObj.put("isshow", isshow);
                                calculatePolicyObj.put("unit", unit);
                                calculatePolicyObj.put("uuid", calculatePolicyUuid);
                                calculatePolicyObj.put("priorityList", priorityList);
                                calculatePolicyObj.put("conditionGroupList", conditionGroupList);
                                calculatePolicyObj.put("conditionGroupRelList", conditionGroupRelList);
                                calculatePolicyArray.add(calculatePolicyObj);
                            }
                        }
                    }
                    slaObj.put("calculatePolicyList", calculatePolicyArray);

                    JSONArray notifyPolicyArray = new JSONArray();
                    JSONArray notifyPolicyList = sla.getJSONArray("notifyPolicyList");
                    if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
                        for (int j = 0; j < notifyPolicyList.size(); j++) {
                            JSONObject notifyPolicy = notifyPolicyList.getJSONObject(j);
                            if (MapUtils.isNotEmpty(notifyPolicy)) {
                                String executeType = notifyPolicy.getString("executeType");
                                String unit = notifyPolicy.getString("unit");
                                String expression = notifyPolicy.getString("expression");
                                String intervalUnit = notifyPolicy.getString("intervalUnit");
                                String notifyPolicyUuid = notifyPolicy.getString("uuid");
                                String time = notifyPolicy.getString("time");
                                String intervalTime = notifyPolicy.getString("intervalTime");
                                JSONObject notifyPolicyConfig = notifyPolicy.getJSONObject("notifyPolicyConfig");
                                JSONObject notifyPolicyConfigObj = ProcessConfigUtil.makeupNotifyPolicyConfig(notifyPolicyConfig, SlaNotifyPolicyHandler.class);
                                JSONObject notifyPolicyObj2 = new JSONObject();
                                notifyPolicyObj2.put("executeType", executeType);
                                notifyPolicyObj2.put("unit", unit);
                                notifyPolicyObj2.put("expression", expression);
                                notifyPolicyObj2.put("intervalUnit", intervalUnit);
                                notifyPolicyObj2.put("uuid", notifyPolicyUuid);
                                notifyPolicyObj2.put("time", time);
                                notifyPolicyObj2.put("intervalTime", intervalTime);
                                notifyPolicyObj2.put("notifyPolicyConfig", notifyPolicyConfigObj);
                                notifyPolicyArray.add(notifyPolicyObj2);
                            }
                        }
                    }
                    slaObj.put("notifyPolicyList", notifyPolicyArray);
                    String slaUuid = sla.getString("uuid");
                    String slaName = sla.getString("name");
                    slaObj.put("uuid", slaUuid);
                    slaObj.put("name", slaName);
                    slaArray.add(slaObj);
                }
            }
        }
        resultObj.put("slaList", slaArray);
        return resultObj;
    }

}
