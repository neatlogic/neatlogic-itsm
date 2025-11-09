/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepTimerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2021/12/27 16:04
 **/
@Component
public class TimerProcessUtilHandler extends ProcessStepInternalHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.TIMER.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        JSONObject resultObj = new JSONObject();
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(currentProcessTaskStepVo.getId());
        if (processTaskStepTimerVo != null) {
            resultObj.put("triggerTime", processTaskStepTimerVo.getTriggerTime());
        }
        return resultObj;
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
            this.put("action", ProcessTaskStepOperationType.STEP_VIEW.getValue());
            this.put("text", ProcessTaskStepOperationType.STEP_VIEW.getText());
            this.put("defaultValue", ProcessTaskStepOperationType.STEP_VIEW.getDefaultValue());
            this.put("acceptList", ProcessTaskStepOperationType.STEP_VIEW.getDefaultValue());
            this.put("groupList", ProcessTaskStepOperationType.STEP_VIEW.getGroupList());
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
        /** 表单场景 **/
        String formSceneUuid = configObj.getString("formSceneUuid");
        String formSceneName = configObj.getString("formSceneName");
        resultObj.put("formSceneUuid", formSceneUuid == null ? "" : formSceneUuid);
        resultObj.put("formSceneName", formSceneName == null ? "" : formSceneName);
        return resultObj;
    }
}
