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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.processconfig.AutomaticCallbackConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticRequestConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticTimeWindowConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import org.springframework.stereotype.Service;

@Service
public class AutomaticConfigRegulateHandler implements IRegulateHandler {
    @Override
    public String getName() {
        return "automaticConfig";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONObject automaticConfig = oldConfigObj.getJSONObject("automaticConfig");
        if (automaticConfig == null) {
            automaticConfig = new JSONObject();
        }
        JSONObject automaticObj = new JSONObject();
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
        /** 表单标签 **/
        String formTag = automaticConfig.getString("formTag");
        automaticObj.put("formTag", formTag == null ? "" : formTag);
        newConfigObj.put("automaticConfig", automaticObj);
    }
}
