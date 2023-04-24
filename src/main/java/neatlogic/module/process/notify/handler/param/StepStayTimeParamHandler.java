/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.lang3.StringUtils;
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
        if (!(notifyTriggerType instanceof ProcessTaskStepNotifyTriggerType)) {
            return null;
        }
        ProcessTaskStepVo stepVo =  processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepVo.getId());
        if (stepVo != null) {
            String status = stepVo.getStatus();
            if (ProcessTaskStatus.PENDING.getValue().equals(status)) {
                Date activeTime = stepVo.getActiveTime();
                if (activeTime != null) {
                    long stayTime = System.currentTimeMillis() - activeTime.getTime();
                    return millisecondsTransferDayHourTimeUnit(stayTime);
                }
            } else if (ProcessTaskStatus.RUNNING.getValue().equals(status)) {
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
                return day + " 天";
            }
            long hour = milliseconds / (60 * 60 * 1000);
            return day + " 天" + hour + "小时";
        } else if (milliseconds >= (60 * 60 * 1000)) {
            return (milliseconds / (60 * 60 * 1000)) + " 小时";
        }
        return null;
    }
}
