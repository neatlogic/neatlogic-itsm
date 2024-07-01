package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import org.springframework.stereotype.Service;

@Service
public class EndProcessUtilHandler extends ProcessStepInternalHandlerBase {

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.END.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {

    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {

    }
    @Override
    public String[] getRegulateKeyList() {
        return new String[]{"processConfig", "formConfig", "scoreConfig", "slaList"};
    }

    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /* 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_UPDATE,
                ProcessTaskOperationType.PROCESSTASK_URGE
        };
        JSONArray authorityList = configObj.getJSONArray("authorityList");
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        return resultObj;
    }

//    @Override
//    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
//        if (configObj == null) {
//            configObj = new JSONObject();
//        }
//        JSONObject resultObj = new JSONObject();
//        /* 流程设置 **/
//        JSONObject processConfig = configObj.getJSONObject("processConfig");
//        JSONObject processObj = regulateProcessConfig(processConfig);
//        resultObj.put("processConfig", processObj);
//        /* 表单设置 **/
//        JSONObject formConfig = configObj.getJSONObject("formConfig");
//        JSONObject formObj = regulateFormConfig(formConfig);
//        resultObj.put("formConfig", formObj);
//        /* 评分设置 **/
//        JSONObject scoreConfig = configObj.getJSONObject("scoreConfig");
//        JSONObject scoreConfigObj = regulateScoreConfig(scoreConfig);
//        resultObj.put("scoreConfig", scoreConfigObj);
//        /* 时效设置 **/
//        JSONArray slaList = configObj.getJSONArray("slaList");
//        JSONArray slaArray = regulateSlaList(slaList);
//        resultObj.put("slaList", slaArray);
//        return resultObj;
//    }
//
//    private JSONObject regulateProcessConfig(JSONObject processConfig) {
//        JSONObject processObj = new JSONObject();
//        if (processConfig == null) {
//            processConfig = new JSONObject();
//        }
//        String uuid = processConfig.getString("uuid");
//        String name = processConfig.getString("name");
//        processObj.put("uuid", uuid);
//        processObj.put("name", name);
//        /* 授权 **/
//        ProcessTaskOperationType[] stepActions = {
//                ProcessTaskOperationType.PROCESSTASK_ABORT,
//                ProcessTaskOperationType.PROCESSTASK_UPDATE,
//                ProcessTaskOperationType.PROCESSTASK_URGE
//        };
//        JSONArray authorityList = null;
//        Integer enableAuthority = processConfig.getInteger("enableAuthority");
//        if (Objects.equals(enableAuthority, 1)) {
//            authorityList = processConfig.getJSONArray("authorityList");
//        } else {
//            enableAuthority = 0;
//        }
//        processObj.put("enableAuthority", enableAuthority);
//        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
//        processObj.put("authorityList", authorityArray);
//
//        /* 通知 **/
//        JSONObject notifyPolicyConfig = processConfig.getJSONObject("notifyPolicyConfig");
//        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
//        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig, TaskNotifyPolicyHandler.class);
//        processObj.put("notifyPolicyConfig", invokeNotifyPolicyConfigVo);
//
//        /* 动作 **/
//        JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
//        ActionConfigVo actionConfigVo = JSON.toJavaObject(actionConfig, ActionConfigVo.class);
//        if (actionConfigVo == null) {
//            actionConfigVo = new ActionConfigVo();
//        }
//        actionConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
//        processObj.put("actionConfig", actionConfigVo);
//
//        Integer enableMarkRepeat = processConfig.getInteger("enableMarkRepeat");
//        enableMarkRepeat = enableMarkRepeat == null ? 0 : enableMarkRepeat;
//        processObj.put("enableMarkRepeat", enableMarkRepeat);
//        return processObj;
//    }
//
//    private JSONObject regulateFormConfig(JSONObject formConfig) {
//        String formUuid = "";
//        String formName = "";
////        List<FormAttributeAuthorityVo> formAuthorityList = new ArrayList<>();
//        if (MapUtils.isNotEmpty(formConfig)) {
//            formUuid = formConfig.getString("uuid");
//            formName = formConfig.getString("name");
////            JSONArray authorityList = formConfig.getJSONArray("authorityList");
////            if (CollectionUtils.isNotEmpty(authorityList)) {
////                authorityList.removeIf(e -> e == null);
////                for (int i = 0; i < authorityList.size(); i++) {
////                    FormAttributeAuthorityVo formAttributeAuthorityVo = authorityList.getObject(i, FormAttributeAuthorityVo.class);
////                    if (formAttributeAuthorityVo != null) {
////                        formAuthorityList.add(formAttributeAuthorityVo);
////                    }
////                }
////            }
//        }
//        JSONObject formObj = new JSONObject();
//        formObj.put("uuid", formUuid);
//        formObj.put("name", formName);
////        formObj.put("authorityList", formAuthorityList);
//        return formObj;
//    }
//
//    private JSONObject regulateScoreConfig(JSONObject scoreConfig) {
//        JSONObject scoreConfigObj = new JSONObject();
//        Integer isActive = 0;
//        if (MapUtils.isNotEmpty(scoreConfig)) {
//            isActive = scoreConfig.getInteger("isActive");
//            if (Objects.equals(isActive, 1)) {
//                Long scoreTemplateId = scoreConfig.getLong("scoreTemplateId");
//                Integer isAuto = scoreConfig.getInteger("isAuto");
//                if (Objects.equals(isAuto, 1)) {
//                    String autoTimeType = "naturalDay";
//                    Integer autoTime = 3;
//                    JSONObject autoConfig = scoreConfig.getJSONObject("config");
//                    if (MapUtils.isNotEmpty(autoConfig)) {
//                        autoTimeType = autoConfig.getString("autoTimeType");
//                        autoTime = autoConfig.getInteger("autoTime");
//                    }
//                    JSONObject autoConfigObj = new JSONObject();
//                    autoConfigObj.put("autoTimeType", autoTimeType);
//                    autoConfigObj.put("autoTime", autoTime);
//                    scoreConfigObj.put("config", autoConfigObj);
//                } else {
//                    isAuto = 0;
//                }
//                scoreConfigObj.put("scoreTemplateId", scoreTemplateId);
//                scoreConfigObj.put("isAuto", isAuto);
//            }
//        }
//        scoreConfigObj.put("isActive", isActive);
//        return scoreConfigObj;
//    }
//
//    private JSONArray regulateSlaList(JSONArray slaList) {
//        JSONArray slaArray = new JSONArray();
//        if (CollectionUtils.isNotEmpty(slaList)) {
//            List<String> effectiveStepUuidList = ProcessMessageManager.getEffectiveStepUuidList();
//            for (int i = 0; i < slaList.size(); i++) {
//                JSONObject sla = slaList.getJSONObject(i);
//                if (MapUtils.isNotEmpty(sla)) {
//                    JSONObject slaObj = new JSONObject();
//                    String slaName = sla.getString("name");
//                    List<String> processStepUuidList = sla.getJSONArray("processStepUuidList").toJavaList(String.class);
//                    if (processStepUuidList == null) {
//                        processStepUuidList = new ArrayList<>();
//                    } else {
//                        processStepUuidList.removeIf(Objects::isNull);
//                    }
//                    List<String> list = ListUtils.removeAll(processStepUuidList, effectiveStepUuidList);
//                    if (CollectionUtils.isNotEmpty(list)) {
//                        throw new RuntimeException("时效设置-时效aaa的关联步骤中存在无效的步骤");
//                    }
////                    if (CollectionUtils.isEmpty(processStepUuidList)) {
////                        continue;
////                    }
////                    processStepUuidList.removeIf(e -> !effectiveStepUuidList.contains(e));
////                    if (CollectionUtils.isEmpty(processStepUuidList)) {
////                        continue;
////                    }
//                    slaObj.put("processStepUuidList", processStepUuidList);
//                    List<SlaTransferPolicyVo> slaTransferPolicyList = new ArrayList<>();
//                    JSONArray transferPolicyList = sla.getJSONArray("transferPolicyList");
//                    if (CollectionUtils.isNotEmpty(transferPolicyList)) {
//                        transferPolicyList.removeIf(Objects::isNull);
//                        for (int j = 0; j < transferPolicyList.size(); j++) {
//                            SlaTransferPolicyVo slaTransferPolicyVo = transferPolicyList.getObject(j, SlaTransferPolicyVo.class);
//                            if (slaTransferPolicyVo != null) {
//                                slaTransferPolicyList.add(slaTransferPolicyVo);
//                            }
//                        }
//                    }
//                    slaObj.put("transferPolicyList", slaTransferPolicyList);
//
//                    List<SlaCalculatePolicyVo> calculatePolicyArray = new ArrayList<>();
//                    JSONArray calculatePolicyList = sla.getJSONArray("calculatePolicyList");
//                    if (CollectionUtils.isNotEmpty(calculatePolicyList)) {
//                        calculatePolicyList.removeIf(Objects::isNull);
//                        for (int j = 0; j < calculatePolicyList.size(); j++) {
//                            SlaCalculatePolicyVo slaCalculatePolicyVo = calculatePolicyList.getObject(j, SlaCalculatePolicyVo.class);
//                            if (slaCalculatePolicyVo != null) {
//                                calculatePolicyArray.add(slaCalculatePolicyVo);
//                            }
//                        }
//                    }
//                    slaObj.put("calculatePolicyList", calculatePolicyArray);
//
//                    List<SlaNotifyPolicyVo> notifyPolicyArray = new ArrayList<>();
//                    JSONArray notifyPolicyList = sla.getJSONArray("notifyPolicyList");
//                    if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
//                        notifyPolicyList.removeIf(Objects::isNull);
//                        for (int j = 0; j < notifyPolicyList.size(); j++) {
//                            SlaNotifyPolicyVo slaNotifyPolicyVo = notifyPolicyList.getObject(j, SlaNotifyPolicyVo.class);
//                            if (slaNotifyPolicyVo != null) {
////                                NotifyPolicyConfigVo notifyPolicyConfigVo = slaNotifyPolicyVo.getNotifyPolicyConfig();
////                                notifyPolicyConfigVo.setHandler(SlaNotifyPolicyHandler.class.getName());
//                                notifyPolicyArray.add(slaNotifyPolicyVo);
//                            }
//                        }
//                    }
//                    slaObj.put("notifyPolicyList", notifyPolicyArray);
//                    String slaUuid = sla.getString("uuid");
//                    String calculateHandler = sla.getString("calculateHandler");
//                    slaObj.put("uuid", slaUuid);
//                    slaObj.put("name", slaName);
//                    slaObj.put("calculateHandler", calculateHandler);
//                    slaArray.add(slaObj);
//                }
//            }
//        }
//        return slaArray;
//    }
}
