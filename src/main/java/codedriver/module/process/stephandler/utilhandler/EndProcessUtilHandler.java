package codedriver.module.process.stephandler.utilhandler;

import java.util.*;

import codedriver.framework.process.dto.processconfig.*;
import codedriver.framework.process.util.ProcessConfigUtil;
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
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_UPDATE,
                ProcessTaskOperationType.PROCESSTASK_URGE
        };
        JSONArray authorityList = null;
        Integer enableAuthority = configObj.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = configObj.getJSONArray("authorityList");
        } else {
            enableAuthority = 0;
        }
        resultObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 通知 **/
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);
        return resultObj;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();
        /** 流程设置 **/
        JSONObject processConfig = configObj.getJSONObject("processConfig");
        JSONObject processObj = regulateProcessConfig(processConfig);
        resultObj.put("processConfig", processObj);
        /** 表单设置 **/
        JSONObject formConfig = configObj.getJSONObject("formConfig");
        JSONObject formObj = regulateFormConfig(formConfig);
        resultObj.put("formConfig", formObj);
        /** 评分设置 **/
        JSONObject scoreConfig = configObj.getJSONObject("scoreConfig");
        JSONObject scoreConfigObj = regulateScoreConfig(scoreConfig);
        resultObj.put("scoreConfig", scoreConfigObj);
        /** 时效设置 **/
        JSONArray slaList = configObj.getJSONArray("slaList");
        JSONArray slaArray = regulateSlaList(slaList);
        resultObj.put("slaList", slaArray);
        return resultObj;
    }

    private JSONObject regulateProcessConfig(JSONObject processConfig) {
        JSONObject processObj = new JSONObject();
        if (processConfig == null) {
            processConfig = new JSONObject();
        }
        String uuid = processConfig.getString("uuid");
        String name = processConfig.getString("name");
        processObj.put("uuid", uuid);
        processObj.put("name", name);
        /** 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_UPDATE,
                ProcessTaskOperationType.PROCESSTASK_URGE
        };
        JSONArray authorityList = null;
        Integer enableAuthority = processConfig.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = processConfig.getJSONArray("authorityList");
        } else {
            enableAuthority = 0;
        }
        processObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        processObj.put("authorityList", authorityArray);

        /** 通知 **/
        JSONObject notifyPolicyConfig = processConfig.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
        processObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

        /** 动作 **/
        JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        actionConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
        processObj.put("actionConfig", actionConfigVo);
        return processObj;
    }

    private JSONObject regulateFormConfig(JSONObject formConfig) {
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

    private JSONObject regulateScoreConfig(JSONObject scoreConfig) {
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

    private JSONArray regulateSlaList(JSONArray slaList) {
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
                            SlaCalculatePolicyVo slaCalculatePolicyVo = calculatePolicyList.getObject(j, SlaCalculatePolicyVo.class);
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
//                                NotifyPolicyConfigVo notifyPolicyConfigVo = slaNotifyPolicyVo.getNotifyPolicyConfig();
//                                notifyPolicyConfigVo.setHandler(SlaNotifyPolicyHandler.class.getName());
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
