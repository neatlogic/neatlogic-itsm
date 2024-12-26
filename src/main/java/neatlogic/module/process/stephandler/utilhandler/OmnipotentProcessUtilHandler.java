package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.processconfig.ActionConfigActionVo;
import neatlogic.framework.process.dto.processconfig.ActionConfigVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stephandler.core.IProcessStepAssistantHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.module.process.notify.handler.OmnipotentNotifyPolicyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OmnipotentProcessUtilHandler extends ProcessStepInternalHandlerBase implements IProcessStepAssistantHandler {

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.OMNIPOTENT.getHandler();
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
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
        defaultUpdateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
    }

    @SuppressWarnings("serial")
    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /* 授权 */
        IOperationType[] stepActions = {
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
        JSONArray authorityList = configObj.getJSONArray("authorityList");
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /* 按钮映射列表 */
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER
        };

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);

        /* 状态映射列表 */
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /* 可替换文本列表 */
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig", taskConfig);
        return resultObj;
    }

    public IOperationType[] getStepActions() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
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
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
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
        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig, OmnipotentNotifyPolicyHandler.class);
        resultObj.put("notifyPolicyConfig", invokeNotifyPolicyConfigVo);

        /** 动作 **/
        JSONObject actionConfig = configObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSON.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        actionConfigVo.setHandler(OmnipotentNotifyPolicyHandler.class.getName());
        resultObj.put("actionConfig", actionConfigVo);

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        /** 按钮映射列表 **/
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER,
                ProcessTaskStepOperationType.STEP_REAPPROVAL
        };
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /** 分配处理人 **/
        JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        resultObj.put("workerPolicyConfig", workerPolicyObj);

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig", taskConfig);

        JSONObject simpleSettings = ProcessConfigUtil.regulateSimpleSettings(configObj);
        resultObj.putAll(simpleSettings);

        /** 重审 **/
        Integer enableReapproval = configObj.getInteger("enableReapproval");
        enableReapproval = enableReapproval == null ? 0 : enableReapproval;
        resultObj.put("enableReapproval", enableReapproval);

        /** 自动流转 **/
        Integer autoComplete = configObj.getInteger("autoComplete");
        autoComplete = autoComplete == null ? 0 : autoComplete;
        resultObj.put("autoComplete", autoComplete);

        /** 自动审批 **/
        Integer autoApproval = configObj.getInteger("autoApproval");
        autoApproval = autoApproval == null ? 0 : autoApproval;
        resultObj.put("autoApproval", autoApproval);
        /** 表单场景 **/
        String formSceneUuid = configObj.getString("formSceneUuid");
        String formSceneName = configObj.getString("formSceneName");
        resultObj.put("formSceneUuid", formSceneUuid == null ? "" : formSceneUuid);
        resultObj.put("formSceneName", formSceneName == null ? "" : formSceneName);
        return resultObj;
    }

    @Override
    public List<ProcessTaskStepUserVo> getMinorUserListForNotifyReceiver(ProcessTaskStepVo currentProcessTaskStepVo) {
        List<ProcessTaskStepUserVo> resultList = new ArrayList<>();
        /* 当前任务处理人 */
        ProcessTaskStepTaskVo stepTaskVo = currentProcessTaskStepVo.getProcessTaskStepTaskVo();
        if (stepTaskVo != null) {
            List<ProcessTaskStepTaskUserVo> taskUserVoList = stepTaskVo.getStepTaskUserVoList();
            if (CollectionUtils.isNotEmpty(taskUserVoList)) {
                for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                    processTaskStepUserVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                    processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                    processTaskStepUserVo.setUserUuid(taskUserVo.getUserUuid());
                    resultList.add(processTaskStepUserVo);
                }
            }
        }
        return resultList;
    }
}
