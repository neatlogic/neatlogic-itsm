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
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.processconfig.AutomaticCallbackConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticIntervalCallbackConfigVo;
import neatlogic.framework.process.dto.processconfig.AutomaticRequestConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dependency.handler.IntegrationProcessStepDependencyHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class AutomaticConfigMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getName() {
        return "automaticConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        JSONObject automaticConfig = stepConfigObj.getJSONObject("automaticConfig");
        if (MapUtils.isNotEmpty(automaticConfig)) {
            if (Objects.equals(action, "save")) {
                JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
                AutomaticRequestConfigVo requestConfigVo = JSONObject.toJavaObject(requestConfig, AutomaticRequestConfigVo.class);
                if (requestConfigVo != null) {
                    String integrationUuid = requestConfigVo.getIntegrationUuid();
                    if (StringUtils.isNotBlank(integrationUuid)) {
                        if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                            throw new IntegrationNotFoundException(integrationUuid);
                        }
                        DependencyManager.insert(IntegrationProcessStepDependencyHandler.class, integrationUuid, processStepVo.getUuid());
                    }
                }

                JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
                AutomaticCallbackConfigVo callbackConfigVo = JSONObject.toJavaObject(callbackConfig, AutomaticCallbackConfigVo.class);
                if (callbackConfigVo != null) {
                    AutomaticIntervalCallbackConfigVo configVo = callbackConfigVo.getConfig();
                    if (configVo != null) {
                        String integrationUuid = configVo.getIntegrationUuid();
                        if (StringUtils.isNotBlank(integrationUuid)) {
                            if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                                throw new IntegrationNotFoundException(integrationUuid);
                            }
                            DependencyManager.insert(IntegrationProcessStepDependencyHandler.class, integrationUuid, processStepVo.getUuid());
                        }
                    }
                }
            } else if (Objects.equals(action, "delete")) {
                DependencyManager.delete(IntegrationProcessStepDependencyHandler.class, processStepVo.getUuid());
            }
        }
    }
}
