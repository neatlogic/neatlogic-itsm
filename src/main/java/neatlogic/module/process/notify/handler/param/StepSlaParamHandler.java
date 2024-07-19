/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class StepSlaParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.PROCESS_TASK_STEP_SLA.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
//        List<String> slaTimeList = new ArrayList<>();
//        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskService.getSlaTimeListByProcessTaskStepId(processTaskStepVo.getId());
//        for (ProcessTaskSlaTimeVo slaTimeVo : processTaskSlaTimeList) {
//            StringBuilder stringBuilder = new StringBuilder(slaTimeVo.getName());
//            stringBuilder.append(" ");
//            if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.DONE.name().toLowerCase())) {
////                stringBuilder.append("耗时 ");
//                stringBuilder.append($.t("耗时"));
//                stringBuilder.append(" ");
//                stringBuilder.append(TimeUtil.millisecondsFormat((slaTimeVo.getTimeSum() - slaTimeVo.getTimeLeft()), 3, TimeUnit.MINUTES, " "));
//                stringBuilder.append(" ");
//                stringBuilder.append($.t("已完成"));
////                stringBuilder.append("完成");
//            } else {
//                if (slaTimeVo.getTimeLeft() > 0) {
////                    stringBuilder.append("剩余 ");
//                    stringBuilder.append($.t("剩余"));
//                    stringBuilder.append(" ");
//                } else {
////                    stringBuilder.append("超时 ");
//                    stringBuilder.append($.t("超时"));
//                    stringBuilder.append(" ");
//                }
//                if (slaTimeVo.getTimeLeft() > 0 || Objects.equals(slaTimeVo.getDisplayModeAfterTimeout(), "workTime")) {
//                    stringBuilder.append(TimeUtil.millisecondsFormat(slaTimeVo.getTimeLeft(), 3, TimeUnit.MINUTES, " "));
//                } else {
//                    stringBuilder.append(TimeUtil.millisecondsFormat((System.currentTimeMillis() - slaTimeVo.getExpireTime().getTime()), 3, TimeUnit.MINUTES, " "));
//                }
//                if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.DOING.name().toLowerCase())) {
//                    stringBuilder.append(" ");
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    stringBuilder.append(sdf.format(slaTimeVo.getExpireTime()));
////                    stringBuilder.append("截止");
//                    stringBuilder.append($.t("截止"));
//                } else if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.PAUSE.name().toLowerCase())) {
//                    stringBuilder.append(" ");
////                    stringBuilder.append("已暂停");
//                    stringBuilder.append($.t("已暂停"));
//                }
//            }
//            slaTimeList.add(stringBuilder.toString());
//        }
//        return slaTimeList;
        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskService.getSlaTimeListByProcessTaskStepId(processTaskStepVo.getId());
        if (CollectionUtils.isEmpty(processTaskSlaTimeList)) {
            return null;
        }
        JSONArray resultList = new JSONArray();
        for (ProcessTaskSlaTimeVo slaTimeVo : processTaskSlaTimeList) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", slaTimeVo.getSlaId());
            jsonObj.put("name", slaTimeVo.getName());
            jsonObj.put("status", slaTimeVo.getStatus());
            jsonObj.put("timeLeft", slaTimeVo.getTimeLeft());
//            if (slaTimeVo.getTimeLeft() > 0 || Objects.equals(slaTimeVo.getDisplayModeAfterTimeout(), "workTime")) {
            if (Objects.equals(slaTimeVo.getSlaTimeDisplayMode(), "workTime")) {
                jsonObj.put("timeLeftFormat", TimeUtil.millisecondsFormat(slaTimeVo.getTimeLeft(), 3, TimeUnit.MINUTES, " "));
            } else {
                jsonObj.put("timeLeftFormat", TimeUtil.millisecondsFormat((System.currentTimeMillis() - slaTimeVo.getExpireTime().getTime()), 3, TimeUnit.MINUTES, " "));
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jsonObj.put("expireTimeFormat", sdf.format(slaTimeVo.getExpireTime()));
            jsonObj.put("timeCostFormat", TimeUtil.millisecondsFormat((slaTimeVo.getTimeSum() - slaTimeVo.getTimeLeft()), 3, TimeUnit.MINUTES, " "));
            resultList.add(jsonObj);
        }
        return resultList;
    }
}
