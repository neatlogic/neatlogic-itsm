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

package neatlogic.module.process.startup.handler;

import neatlogic.framework.common.config.Config;
import neatlogic.framework.startup.StartupBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ClearProcessTaskStepInOperationStartupHandler extends StartupBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpsh.clearprocesstaskstepinoperationstartuphandler.getname";
    }

    @Override
    public int executeForCurrentTenant() {
        return processTaskMapper.deleteProcessTaskStepInOperationByServerId((long) Config.SCHEDULE_SERVER_ID);
    }

    @Override
    public int sort() {
        return 12;
    }
}
