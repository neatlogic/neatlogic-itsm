/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
