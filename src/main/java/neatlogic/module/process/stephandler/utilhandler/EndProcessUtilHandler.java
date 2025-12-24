package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.util.TimeUtil;
import neatlogic.framework.worktime.dto.WorktimeRangeVo;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.framework.worktime.exception.WorktimeRangeNotFoundException;
import neatlogic.module.process.notify.handler.TaskNotifyPolicyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {

    }

    @Override
    public Class<? extends INotifyPolicyHandler> getNotifyPolicyHandlerClass() {
        return TaskNotifyPolicyHandler.class;
    }

    @Override
    public String[] getRegulateKeyList() {
        return new String[]{"processConfig", "formConfig", "scoreConfig", "slaList"};
    }

    @Override
    protected void myCheckDependenciesBeforeReport(JSONObject configObj) {
        JSONArray slaList = configObj.getJSONArray("slaList");
        checkSla(slaList);
    }

    private void checkSla(JSONArray slaList) {
        if (CollectionUtils.isNotEmpty(slaList)) {
            long max = 0;
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject slaObj = slaList.getJSONObject(i);
                if (MapUtils.isNotEmpty(slaObj)) {
                    JSONArray calculatePolicyList = slaObj.getJSONArray("calculatePolicyList");
                    if (CollectionUtils.isNotEmpty(calculatePolicyList)) {
                        for (int j = 0; j < calculatePolicyList.size(); j++) {
                            JSONObject calculatePolicyObj = calculatePolicyList.getJSONObject(j);
                            if (MapUtils.isNotEmpty(calculatePolicyObj)) {
                                Integer enablePriority = calculatePolicyObj.getInteger("enablePriority");
                                if (Objects.equals(enablePriority, 1)) {
                                    JSONArray priorityList = calculatePolicyObj.getJSONArray("priorityList");
                                    if (CollectionUtils.isNotEmpty(priorityList)) {
                                        for (int k = 0; k < priorityList.size(); k++) {
                                            JSONObject priorityObj = priorityList.getJSONObject(k);
                                            if (MapUtils.isNotEmpty(priorityObj)) {
                                                Integer time = priorityObj.getInteger("time");
                                                String unit = priorityObj.getString("unit");
                                                if (time != null && StringUtils.isNotBlank(unit)) {
                                                    if (unit.equalsIgnoreCase("day")) {
                                                        long millis = TimeUnit.DAYS.toMillis(time);
                                                        max = Math.max(millis, max);
                                                    } else if (unit.equalsIgnoreCase("hour")) {
                                                        long millis = TimeUnit.HOURS.toMillis(time);
                                                        max = Math.max(millis, max);
                                                    } else {
                                                        long millis = TimeUnit.MINUTES.toMillis(time);
                                                        max = Math.max(millis, max);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Integer time = calculatePolicyObj.getInteger("time");
                                    String unit = calculatePolicyObj.getString("unit");
                                    if (time != null && StringUtils.isNotBlank(unit)) {
                                        if (unit.equalsIgnoreCase("day")) {
                                            long millis = TimeUnit.DAYS.toMillis(time);
                                            max = Math.max(millis, max);
                                        } else if (unit.equalsIgnoreCase("hour")) {
                                            long millis = TimeUnit.HOURS.toMillis(time);
                                            max = Math.max(millis, max);
                                        } else {
                                            long millis = TimeUnit.MINUTES.toMillis(time);
                                            max = Math.max(millis, max);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (max > 0) {
                WorktimeRangeVo lastWorktimeRange = ProcessMessageManager.getLastWorktimeRange();
                if (lastWorktimeRange.getEndTime() < (System.currentTimeMillis() + max)) {
                    WorktimeVo worktime = ProcessMessageManager.getWorktime();
                    String format = Instant.ofEpochMilli(lastWorktimeRange.getEndTime()).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern(TimeUtil.YYYY_MM_DD_HH_MM));
                    throw new WorktimeRangeNotFoundException(worktime.getName(), lastWorktimeRange.getYear(), format);
                }
            }
        }
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
