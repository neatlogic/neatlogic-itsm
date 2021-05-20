package codedriver.module.process.stephandler.utilhandler;

import java.util.*;

import codedriver.framework.process.dto.processconfig.*;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.module.process.notify.constvalue.SlaNotifyTriggerType;
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
        JSONObject processConfig = configObj.getJSONObject("processConfig");
        JSONObject processObj = makeupProcessConfig(processConfig);
        resultObj.put("processConfig", processObj);
        /** 表单设置 **/
        JSONObject formConfig = configObj.getJSONObject("formConfig");
        JSONObject formObj = makeupFormConfig(formConfig);
        resultObj.put("formConfig", formObj);
        /** 评分设置 **/
        JSONObject scoreConfig = configObj.getJSONObject("scoreConfig");
        JSONObject scoreConfigObj = makeupScoreConfig(scoreConfig);
        resultObj.put("scoreConfig", scoreConfigObj);
        /** 时效设置 **/
        JSONArray slaList = configObj.getJSONArray("slaList");
        JSONArray slaArray = makeupSlaList(slaList);
        resultObj.put("slaList", slaArray);
        return resultObj;
    }

    private JSONObject makeupProcessConfig(JSONObject processConfig) {
        String uuid = "";
        String name = "";
        JSONArray authorityArray = new JSONArray();
//        JSONObject notifyPolicyObj = new JSONObject();
        NotifyPolicyConfigVo notifyPolicyConfigVo = null;
//        JSONObject actionObj = new JSONObject();
        ActionConfigVo actionConfigVo = null;
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
//                notifyPolicyObj = ProcessConfigUtil.makeupNotifyPolicyConfig(notifyPolicyConfig, TaskNotifyPolicyHandler.class);
                notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
                notifyPolicyConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
            }

            /** 动作 **/
            JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
            if (MapUtils.isNotEmpty(actionConfig)) {
//                actionObj = ProcessConfigUtil.makeupActionConfig(actionConfig, TaskNotifyPolicyHandler.class);
                actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
                actionConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
            }
        }
        JSONObject processObj = new JSONObject();
        processObj.put("uuid", uuid);
        processObj.put("name", name);
        processObj.put("authorityList", authorityArray);
//        processObj.put("notifyPolicyConfig", notifyPolicyObj);
        processObj.put("notifyPolicyConfig", notifyPolicyConfigVo);
//        processObj.put("actionConfig", actionObj);
        processObj.put("actionConfig", actionConfigVo);
        return processObj;
    }

    private JSONObject makeupFormConfig(JSONObject formConfig) {
        String formUuid = "";
        List<FormAttributeAuthorityVo> formAuthorityList = new ArrayList<>();
        if (MapUtils.isNotEmpty(formConfig)) {
            formUuid = formConfig.getString("uuid");
            JSONArray authorityList = formConfig.getJSONArray("authorityList");
            if (CollectionUtils.isNotEmpty(authorityList)) {
                authorityList.removeIf(e -> e == null);
                for (int i = 0; i < authorityList.size(); i++) {
                    FormAttributeAuthorityVo formAttributeAuthorityVo = authorityList.getObject(i, FormAttributeAuthorityVo.class);
                    if (formAttributeAuthorityVo != null) {
                        formAuthorityList.add(formAttributeAuthorityVo);
                    }
                }
            }
        }
        JSONObject formObj = new JSONObject();
        formObj.put("uuid", formUuid);
        formObj.put("authorityList", formAuthorityList);
        return formObj;
    }

    private JSONObject makeupScoreConfig(JSONObject scoreConfig) {
        JSONObject scoreConfigObj = new JSONObject();
        Integer isActive = 0;
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
        return scoreConfigObj;
    }

    private JSONArray makeupSlaList(JSONArray slaList) {
        JSONArray slaArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(slaList)) {
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject sla = slaList.getJSONObject(i);
                if (MapUtils.isNotEmpty(sla)) {
                    JSONObject slaObj = new JSONObject();
                    List<SlaTransferPolicyVo> slaTransferPolicyList = new ArrayList<>();
                    JSONArray transferPolicyList = sla.getJSONArray("transferPolicyList");
                    if (CollectionUtils.isNotEmpty(transferPolicyList)) {
                        transferPolicyList.removeIf(e -> e == null);
                        for (int j = 0; j < transferPolicyList.size(); j++) {
                            SlaTransferPolicyVo slaTransferPolicyVo = transferPolicyList.getObject(j, SlaTransferPolicyVo.class);
                            if (slaTransferPolicyVo != null) {
                                slaTransferPolicyList.add(slaTransferPolicyVo);
                            }
                        }
                    }
                    slaObj.put("transferPolicyList", slaTransferPolicyList);

                    List<String> processStepUuidList = sla.getJSONArray("processStepUuidList").toJavaList(String.class);
                    if (processStepUuidList == null) {
                        processStepUuidList = new ArrayList();
                    } else {
                        processStepUuidList.removeIf(e -> e == null);
                    }
                    slaObj.put("processStepUuidList", processStepUuidList);

                    List<SlaCalculatePolicyVo> calculatePolicyArray = new ArrayList<>();
                    JSONArray calculatePolicyList = sla.getJSONArray("calculatePolicyList");
                    if (CollectionUtils.isNotEmpty(calculatePolicyList)) {
                        calculatePolicyList.removeIf(e -> e == null);
                        for (int j = 0; j < calculatePolicyList.size(); j++) {
                            SlaCalculatePolicyVo slaCalculatePolicyVo = calculatePolicyList.getObject(i, SlaCalculatePolicyVo.class);
                            if (slaCalculatePolicyVo != null) {
                                calculatePolicyArray.add(slaCalculatePolicyVo);
                            }
                        }
                    }
                    slaObj.put("calculatePolicyList", calculatePolicyArray);

                    List<SlaNotifyPolicyVo> notifyPolicyArray = new ArrayList<>();
                    JSONArray notifyPolicyList = sla.getJSONArray("notifyPolicyList");
                    if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
                        notifyPolicyList.removeIf(e -> e == null);
                        for (int j = 0; j < notifyPolicyList.size(); j++) {
                            SlaNotifyPolicyVo slaNotifyPolicyVo = notifyPolicyList.getObject(j, SlaNotifyPolicyVo.class);
                            if (slaNotifyPolicyVo != null) {
                                NotifyPolicyConfigVo notifyPolicyConfigVo = slaNotifyPolicyVo.getNotifyPolicyConfig();
                                notifyPolicyConfigVo.setHandler(SlaNotifyPolicyHandler.class.getName());
//                                JSONObject notifyPolicyConfigObj = ProcessConfigUtil.makeupNotifyPolicyConfig(notifyPolicyConfig, SlaNotifyPolicyHandler.class);
//                                slaNotifyPolicyVo.setNotifyPolicyConfig(notifyPolicyConfigObj);
                                notifyPolicyArray.add(slaNotifyPolicyVo);
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
        return slaArray;
    }
}
