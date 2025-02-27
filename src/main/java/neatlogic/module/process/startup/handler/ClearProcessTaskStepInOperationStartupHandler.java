/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
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
