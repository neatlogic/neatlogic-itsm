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
import neatlogic.framework.process.util.ProcessConfigUtil;
import org.springframework.stereotype.Service;

@Service
public class WorkerPolicyConfigRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "workerPolicyConfig";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONObject workerPolicyConfig = oldConfigObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        newConfigObj.put("workerPolicyConfig", workerPolicyObj);
    }
}
