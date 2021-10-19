/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepVo stepVo =  processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepVo.getId());
        if (stepVo != null) {
            String status = stepVo.getStatus();
            if (ProcessTaskStatus.PENDING.getValue().equals(status)) {
                Date activeTime = stepVo.getActiveTime();
                if (activeTime != null) {
                    long stayTime = System.currentTimeMillis() - activeTime.getTime();
                    stayTime = 26 * 60 * 60 * 1000 + 123;
                    return millisecondsTransferDayHourTimeUnit(stayTime);
                }
            } else if (ProcessTaskStatus.RUNNING.getValue().equals(status)) {
                Date startTime = stepVo.getStartTime();
                if (startTime != null) {
                    long stayTime = System.currentTimeMillis() - startTime.getTime();
                    stayTime = 26 * 60 * 60 * 1000 + 123;
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
