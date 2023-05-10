/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

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
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class StartDateParamHandler extends ProcessTaskNotifyParamHandlerBase {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.STARTDATE.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            Date startTime = processTaskVo.getStartTime();
            if (startTime != null) {
                return sdf.format(startTime);
            }
        }
        return null;
    }
}
