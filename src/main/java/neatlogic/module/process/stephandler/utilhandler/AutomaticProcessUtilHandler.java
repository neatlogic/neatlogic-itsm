package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.processconfig.AutomaticCallbackConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticRequestConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticTimeWindowConfigVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.notify.handler.AutomaticNotifyPolicyHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class AutomaticProcessUtilHandler extends ProcessStepInternalHandlerBase {
    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.AUTOMATIC.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                .getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),
                        currentProcessTaskStepVo.getId(), currentProcessTaskStepVo.getHandler(), SystemUser.SYSTEM.getUserUuid()));
        JSONObject stepDataJson = stepDataVo.getData();
        boolean hasComplete =
                new ProcessAuthManager.StepOperationChecker(currentProcessTaskStepVo.getId(), ProcessTaskStepOperationType.STEP_COMPLETE)
                        .build().check();
        if (hasComplete) {// 有处理权限
            stepDataJson.put("isStepUser", 1);
            if (currentProcessTaskStepVo.getHandler().equals(ProcessStepHandlerType.AUTOMATIC.getHandler())) {
                JSONObject requestAuditJson = stepDataJson.getJSONObject("requestAudit");
                if (requestAuditJson.containsKey("status") && requestAuditJson.getJSONObject("status")
                        .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                    requestAuditJson.put("isRetry", 1);
                } else {
                    requestAuditJson.put("isRetry", 0);
                }
                JSONObject callbackAuditJson = stepDataJson.getJSONObject("callbackAudit");
                if (callbackAuditJson != null) {
                    if (callbackAuditJson.containsKey("status") && callbackAuditJson.getJSONObject("status")
                            .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                        callbackAuditJson.put("isRetry", 1);
                    } else {
                        callbackAuditJson.put("isRetry", 0);
                    }
                }
            }
        }
        return stepDataJson;
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
    }

    @SuppressWarnings("serial")
    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        IOperationType[] stepActions = {
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER
        };
        JSONArray authorityList = configObj.getJSONArray("authorityList");
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 按钮映射 **/
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT
        };
        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        return resultObj;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        IOperationType[] stepActions = {
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER
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
        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig, AutomaticNotifyPolicyHandler.class);
        resultObj.put("notifyPolicyConfig", invokeNotifyPolicyConfigVo);

        /** 按钮映射列表 **/
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT
        };
        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /** 自动化配置 **/
        JSONObject automaticObj = new JSONObject();
        JSONObject automaticConfig = configObj.getJSONObject("automaticConfig");
        if (automaticConfig == null) {
            automaticConfig = new JSONObject();
        }
        /** 外部调用 **/
        JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
        AutomaticRequestConfigVo requestConfigVo = JSONObject.toJavaObject(requestConfig, AutomaticRequestConfigVo.class);
        if (requestConfigVo == null) {
            requestConfigVo = new AutomaticRequestConfigVo();
        }
        automaticObj.put("requestConfig", requestConfigVo);
        /** 是否回调 **/
        JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
        AutomaticCallbackConfigVo callbackConfigVo = JSONObject.toJavaObject(callbackConfig, AutomaticCallbackConfigVo.class);
        if (callbackConfigVo == null) {
            callbackConfigVo = new AutomaticCallbackConfigVo();
        }
        automaticObj.put("callbackConfig", callbackConfigVo);
        /** 时间窗口 **/
        JSONObject timeWindowConfig = automaticConfig.getJSONObject("timeWindowConfig");
        AutomaticTimeWindowConfigVo timeWindowConfigVo = JSONObject.toJavaObject(timeWindowConfig, AutomaticTimeWindowConfigVo.class);
        if (timeWindowConfigVo == null) {
            timeWindowConfigVo = new AutomaticTimeWindowConfigVo();
        }
        automaticObj.put("timeWindowConfig", timeWindowConfigVo);
        resultObj.put("automaticConfig", automaticObj);
        /** 分配处理人 **/
        JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        resultObj.put("workerPolicyConfig", workerPolicyObj);

        JSONArray tagList = configObj.getJSONArray("tagList");
        if (tagList == null) {
            tagList = new JSONArray();
        }
        resultObj.put("tagList", tagList);
        /** 表单场景 **/
        String formSceneUuid = configObj.getString("formSceneUuid");
        String formSceneName = configObj.getString("formSceneName");
        resultObj.put("formSceneUuid", formSceneUuid == null ? "" : formSceneUuid);
        resultObj.put("formSceneName", formSceneName == null ? "" : formSceneName);
        /** 表单标签 **/
        String formTag = configObj.getString("formTag");
        resultObj.put("formTag", formTag == null ? "" : formTag);
        return resultObj;
    }

}
