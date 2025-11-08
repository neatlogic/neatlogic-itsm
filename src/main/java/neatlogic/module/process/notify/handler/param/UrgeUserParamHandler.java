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

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UrgeUserParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.PROCESS_TASK_URGE_USER.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskStepNotifyTriggerType.URGE)) {
            return null;
        }
        String userUuid = processTaskMapper.getProcessTaskLastUrgeUserUuidByProcessTaskId(processTaskStepVo.getProcessTaskId());
        UserVo userVo = userMapper.getUserBaseInfoByUuid(userUuid);
        if (userVo != null) {
            return userVo.getUserName() + "(" + userVo.getUserId() + ")";
        }
        return null;
    }
}
