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

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class StepStartTimeParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.PROCESS_TASK_STEP_START_TIME.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = processTaskStepVo.getStartTime();
        if (startTime != null) {
            return sdf.format(startTime);
        }
        Long id = processTaskStepVo.getId();
        if (id != null) {
            ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(id);
            if (stepVo != null) {
                startTime = stepVo.getStartTime();
                if (startTime != null) {
                    return sdf.format(startTime);
                }
            }
        }
        return null;
    }
}
