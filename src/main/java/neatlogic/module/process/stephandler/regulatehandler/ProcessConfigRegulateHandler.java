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

package neatlogic.module.process.stephandler.regulatehandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.processconfig.ActionConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.module.process.notify.handler.TaskNotifyPolicyHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProcessConfigRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "processConfig";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONObject processConfig = oldConfigObj.getJSONObject("processConfig");
        JSONObject processObj = new JSONObject();
        if (processConfig == null) {
            processConfig = new JSONObject();
        }
        String uuid = processConfig.getString("uuid");
        String name = processConfig.getString("name");
        processObj.put("uuid", uuid);
        processObj.put("name", name);
        /* 授权 **/
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

        /* 通知 **/
        JSONObject notifyPolicyConfig = processConfig.getJSONObject("notifyPolicyConfig");
        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig, TaskNotifyPolicyHandler.class);
        processObj.put("notifyPolicyConfig", invokeNotifyPolicyConfigVo);

        /* 动作 **/
        JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSON.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        actionConfigVo.setHandler(TaskNotifyPolicyHandler.class.getName());
        processObj.put("actionConfig", actionConfigVo);

        Integer enableMarkRepeat = processConfig.getInteger("enableMarkRepeat");
        enableMarkRepeat = enableMarkRepeat == null ? 0 : enableMarkRepeat;
        processObj.put("enableMarkRepeat", enableMarkRepeat);
        newConfigObj.put("processConfig", processObj);
    }
}
