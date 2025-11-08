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
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.process.dto.processconfig.ActionConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import org.springframework.stereotype.Service;

@Service
public class ActionConfigRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "actionConfig";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONObject actionConfig = oldConfigObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSON.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        Class<? extends INotifyPolicyHandler> clazz = processStepInternalHandler.getNotifyPolicyHandlerClass();
        if (clazz != null) {
            actionConfigVo.setHandler(clazz.getName());
        }
        newConfigObj.put("actionConfig", actionConfigVo);
    }
}
