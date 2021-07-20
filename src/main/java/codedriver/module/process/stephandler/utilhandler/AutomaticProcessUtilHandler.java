package codedriver.module.process.stephandler.utilhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import codedriver.framework.process.dto.processconfig.AutomaticCallbackConfigVo;
import codedriver.framework.process.dto.processconfig.AutomaticRequestConfigVo;
import codedriver.framework.process.dto.processconfig.AutomaticTimeWindowConfigVo;
import codedriver.framework.process.dto.processconfig.NotifyPolicyConfigVo;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.module.process.notify.handler.AutomaticNotifyPolicyHandler;
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
        if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
            Long policyId = notifyPolicyConfig.getLong("policyId");
            if (policyId != null) {
                processStepVo.setNotifyPolicyId(policyId);
            }
        }


        JSONArray actionList = (JSONArray) JSONPath.read(stepConfigObj.toJSONString(), "actionConfig.actionList");
        if (CollectionUtils.isNotEmpty(actionList)) {
            for (int i = 0; i < actionList.size(); i++) {
                JSONObject ationObj = actionList.getJSONObject(i);
                String integrationUuid = ationObj.getString("integrationUuid");
                if (StringUtils.isNotBlank(integrationUuid)) {
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
        if (MapUtils.isNotEmpty(automaticConfig)) {
            JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
            if (MapUtils.isNotEmpty(requestConfig)) {
                String integrationUuid = requestConfig.getString("integrationUuid");
                if (StringUtils.isNotBlank(integrationUuid)) {
                    processStepVo.getIntegrationUuidList().add(integrationUuid);
                }
            }
            JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
            if (MapUtils.isNotEmpty(callbackConfig)) {
                JSONObject config = callbackConfig.getJSONObject("config");
                if (MapUtils.isNotEmpty(config)) {
                    String integrationUuid = config.getString("integrationUuid");
                    if (StringUtils.isNotBlank(integrationUuid)) {
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
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER
        };
        JSONArray authorityList = null;
        Integer enableAuthority = configObj.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = configObj.getJSONArray("authorityList");
        } else {
            enableAuthority = 1;
        }
        resultObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 按钮映射 **/
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.TASK_TRANSFER,
                ProcessTaskOperationType.STEP_START
        };
        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);

        /** 通知 **/
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(AutomaticNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

        return resultObj;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER
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
        notifyPolicyConfigVo.setHandler(AutomaticNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

        /** 按钮映射列表 **/
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.TASK_TRANSFER,
                ProcessTaskOperationType.STEP_START
        };
        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

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
        return resultObj;
    }

}
