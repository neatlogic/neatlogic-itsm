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
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dependency.handler.NotifyPolicyProcessStepDependencyHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class NotifyPolicyConfigMakeupHandler implements IProcessStepMakeupHandler {
    @Override
    public String getName() {
        return "notifyPolicyConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        /* 组装通知策略id **/
        InvokeNotifyPolicyConfigVo notifyPolicyConfig = stepConfigObj.getObject("notifyPolicyConfig", InvokeNotifyPolicyConfigVo.class);
        if (notifyPolicyConfig != null) {
            if (Objects.equals(action, "save")) {
                INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
                if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, notifyPolicyConfig.getPolicyId(), processStepVo.getUuid());
                }
            } else if (Objects.equals(action, "delete")) {
                if (Objects.equals(notifyPolicyConfig.getIsCustom(), 1)) {
                    DependencyManager.delete(NotifyPolicyProcessStepDependencyHandler.class, processStepVo.getUuid());
                }
            }
        }
    }
}
