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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.processconfig.ActionConfigActionVo;
import neatlogic.framework.process.dto.processconfig.ActionConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dependency.handler.IntegrationProcessStepDependencyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class ActionConfigMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getName() {
        return "actionConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        /* 组装动作 **/
        JSONObject actionConfig = stepConfigObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSON.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo != null) {
            List<ActionConfigActionVo> actionList = actionConfigVo.getActionList();
            if (CollectionUtils.isNotEmpty(actionList)) {
                if (Objects.equals(action, "save")) {
                    for (ActionConfigActionVo actionVo : actionList) {
                        String integrationUuid = actionVo.getIntegrationUuid();
                        if (StringUtils.isNotBlank(integrationUuid)) {
                            if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                                throw new IntegrationNotFoundException(integrationUuid);
                            }
                            DependencyManager.insert(IntegrationProcessStepDependencyHandler.class, integrationUuid, processStepVo.getUuid());
                        }
                    }
                } else if (Objects.equals(action, "delete")) {
                    DependencyManager.delete(IntegrationProcessStepDependencyHandler.class, processStepVo.getUuid());
                }
            }
        }
    }
}
