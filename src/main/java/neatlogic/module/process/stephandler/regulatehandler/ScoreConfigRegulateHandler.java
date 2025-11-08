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
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ScoreConfigRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "scoreConfig";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONObject scoreConfig = oldConfigObj.getJSONObject("scoreConfig");
        JSONObject scoreConfigObj = new JSONObject();
        Integer isActive = 0;
        if (MapUtils.isNotEmpty(scoreConfig)) {
            isActive = scoreConfig.getInteger("isActive");
            if (Objects.equals(isActive, 1)) {
                Long scoreTemplateId = scoreConfig.getLong("scoreTemplateId");
                Integer isAuto = scoreConfig.getInteger("isAuto");
                if (Objects.equals(isAuto, 1)) {
                    String autoTimeType = "naturalDay";
                    Integer autoTime = 3;
                    JSONObject autoConfig = scoreConfig.getJSONObject("config");
                    if (MapUtils.isNotEmpty(autoConfig)) {
                        autoTimeType = autoConfig.getString("autoTimeType");
                        autoTime = autoConfig.getInteger("autoTime");
                    }
                    JSONObject autoConfigObj = new JSONObject();
                    autoConfigObj.put("autoTimeType", autoTimeType);
                    autoConfigObj.put("autoTime", autoTime);
                    scoreConfigObj.put("config", autoConfigObj);
                } else {
                    isAuto = 0;
                }
                scoreConfigObj.put("scoreTemplateId", scoreTemplateId);
                scoreConfigObj.put("isAuto", isAuto);
            }
        }
        scoreConfigObj.put("isActive", isActive);
        newConfigObj.put("scoreConfig", scoreConfigObj);
    }
}
