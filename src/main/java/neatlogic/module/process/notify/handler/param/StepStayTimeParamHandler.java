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

import neatlogic.framework.util.$;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class StepStayTimeParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.STEPSTAYTIME.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
            return null;
        }
        ProcessTaskStepVo stepVo =  processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepVo.getId());
        if (stepVo != null) {
            String status = stepVo.getStatus();
            if (ProcessTaskStepStatus.PENDING.getValue().equals(status)) {
                Date activeTime = stepVo.getActiveTime();
                if (activeTime != null) {
                    long stayTime = System.currentTimeMillis() - activeTime.getTime();
                    return millisecondsTransferDayHourTimeUnit(stayTime);
                }
            } else if (ProcessTaskStepStatus.RUNNING.getValue().equals(status)) {
                Date startTime = stepVo.getStartTime();
                if (startTime != null) {
                    long stayTime = System.currentTimeMillis() - startTime.getTime();
                    return millisecondsTransferDayHourTimeUnit(stayTime);
                }
            }
        }
        return null;
    }

    /**
     * @param milliseconds 毫秒数
     * @return String
     * @description : 将毫秒转换换为最大两个单位显示文案
     * @since 2020年9月14日
     */
    public static String millisecondsTransferDayHourTimeUnit(long milliseconds) {
        if (milliseconds >= 24 * 60 * 60 * 1000) {
            long day = milliseconds / (24 * 60 * 60 * 1000);
            milliseconds = milliseconds % (24 * 60 * 60 * 1000);
            if (milliseconds < (60 * 60 * 1000)) {
                return $.t("nmpnh.stepstaytimeparamhandler.duration.days", day);
            }
            long hour = milliseconds / (60 * 60 * 1000);
            return $.t("nmpnh.stepstaytimeparamhandler.duration.dayshours", day, hour);
        } else if (milliseconds >= (60 * 60 * 1000)) {
            return $.t("nmpnh.stepstaytimeparamhandler.duration.hours", milliseconds / (60 * 60 * 1000));
        }
        return null;
    }
}
