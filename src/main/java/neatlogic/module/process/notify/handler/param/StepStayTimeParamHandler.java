/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
