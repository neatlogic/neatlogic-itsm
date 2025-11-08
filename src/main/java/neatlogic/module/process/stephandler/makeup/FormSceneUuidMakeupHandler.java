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

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dependency.handler.FormScene2ProcessStepDependencyHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FormSceneUuidMakeupHandler implements IProcessStepMakeupHandler {
    @Override
    public String getName() {
        return "formSceneUuid";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        // 保存表单场景
        String formSceneUuid = stepConfigObj.getString("formSceneUuid");
        if (StringUtils.isNotBlank(formSceneUuid)) {
            if (Objects.equals(action, "save")) {
                JSONObject config = new JSONObject();
                config.put("processUuid", processStepVo.getProcessUuid());
                config.put("stepUuid", processStepVo.getUuid());
                config.put("stepName", processStepVo.getName());
                DependencyManager.insert(FormScene2ProcessStepDependencyHandler.class, formSceneUuid, processStepVo.getUuid(), config);
            } else if (Objects.equals(action, "delete")) {
                DependencyManager.delete(FormScene2ProcessStepDependencyHandler.class, processStepVo.getUuid());
            }
        }
    }
}
