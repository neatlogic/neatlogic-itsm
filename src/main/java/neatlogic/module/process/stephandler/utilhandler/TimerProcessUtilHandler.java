/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.stephandler.utilhandler;

import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepTimerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linbq
 * @since 2021/12/27 16:04
 **/
@Component
public class TimerProcessUtilHandler extends ProcessStepInternalHandlerBase {
    @Override
    public String getHandler() {
        return ProcessStepHandlerType.TIMER.getHandler();
    }

    @Override
    public Object getHandlerStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public Object getHandlerStepInitInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        JSONObject resultObj = new JSONObject();
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(currentProcessTaskStepVo.getId());
        if (processTaskStepTimerVo != null) {
            resultObj.put("triggerTime", processTaskStepTimerVo.getTriggerTime());
        }
        return resultObj;
    }

    @Override
    public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
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
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {

    }

    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        return new JSONObject();
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();
        /* 默认所有人都可以查看定时节点步骤信息 */
        resultObj.put("enableAuthority", 1);
        JSONArray authorityArray = new JSONArray();
        authorityArray.add(new JSONObject() {{
            this.put("action", ProcessTaskOperationType.STEP_VIEW.getValue());
            this.put("text", ProcessTaskOperationType.STEP_VIEW.getText());
            this.put("defaultValue", ProcessTaskOperationType.STEP_VIEW.getDefaultValue());
            this.put("acceptList", ProcessTaskOperationType.STEP_VIEW.getDefaultValue());
            this.put("groupList", ProcessTaskOperationType.STEP_VIEW.getGroupList());
        }});
        resultObj.put("authorityList", authorityArray);
        /** 分配处理人 **/
        JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        resultObj.put("workerPolicyConfig", workerPolicyObj);

        String type = configObj.getString("type");
        if (StringUtils.isBlank(type)) {
            type = "form";
        }
        resultObj.put("type", type);
        String value = configObj.getString("value");
        if (value == null) {
            value = "";
        }
        resultObj.put("value", value);
        String attributeUuid = configObj.getString("attributeUuid");
        if (attributeUuid == null) {
            attributeUuid = "";
        }
        resultObj.put("attributeUuid", attributeUuid);
        return resultObj;
    }
}
