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
            INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
            if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                if (Objects.equals(action, "save")) {
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, notifyPolicyConfig.getPolicyId(), processStepVo.getUuid());
                } else if (Objects.equals(action, "delete")) {
                    DependencyManager.delete(NotifyPolicyProcessStepDependencyHandler.class, processStepVo.getUuid());
                }
            }
        }
    }
}
