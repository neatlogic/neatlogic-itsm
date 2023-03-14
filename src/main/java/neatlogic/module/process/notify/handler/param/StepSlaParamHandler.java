/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.I18nUtils;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        List<String> slaTimeList = new ArrayList<>();
        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskService.getSlaTimeListByProcessTaskStepId(processTaskStepVo.getId());
        for (ProcessTaskSlaTimeVo slaTimeVo : processTaskSlaTimeList) {
            StringBuilder stringBuilder = new StringBuilder(slaTimeVo.getName());
            stringBuilder.append(" ");
            if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.DONE.name().toLowerCase())) {
//                stringBuilder.append("耗时 ");
                stringBuilder.append(I18nUtils.getMessage("const.timeconsuming"));
                stringBuilder.append(" ");
                stringBuilder.append(TimeUtil.millisecondsFormat((slaTimeVo.getTimeSum() - slaTimeVo.getTimeLeft()), 3, TimeUnit.MINUTES, " "));
                stringBuilder.append(" ");
                stringBuilder.append(I18nUtils.getMessage("const.completed"));
//                stringBuilder.append("完成");
            } else {
                if (slaTimeVo.getTimeLeft() > 0) {
//                    stringBuilder.append("剩余 ");
                    stringBuilder.append(I18nUtils.getMessage("const.timeremaining"));
                    stringBuilder.append(" ");
                } else {
//                    stringBuilder.append("超时 ");
                    stringBuilder.append(I18nUtils.getMessage("const.overtime"));
                    stringBuilder.append(" ");
                }
                if (slaTimeVo.getTimeLeft() > 0 || Objects.equals(slaTimeVo.getDisplayModeAfterTimeout(), "workTime")) {
                    stringBuilder.append(TimeUtil.millisecondsFormat(slaTimeVo.getTimeLeft(), 3, TimeUnit.MINUTES, " "));
                } else {
                    stringBuilder.append(TimeUtil.millisecondsFormat((System.currentTimeMillis() - slaTimeVo.getExpireTime().getTime()), 3, TimeUnit.MINUTES, " "));
                }
                if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.DOING.name().toLowerCase())) {
                    stringBuilder.append(" ");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    stringBuilder.append(sdf.format(slaTimeVo.getExpireTime()));
//                    stringBuilder.append("截止");
                    stringBuilder.append(I18nUtils.getMessage("const.deadline"));
                } else if (Objects.equals(slaTimeVo.getStatus(), SlaStatus.PAUSE.name().toLowerCase())) {
                    stringBuilder.append(" ");
//                    stringBuilder.append("已暂停");
                    stringBuilder.append(I18nUtils.getMessage("const.paused"));
                }
            }
            slaTimeList.add(stringBuilder.toString());
        }
        return slaTimeList;
    }
}
