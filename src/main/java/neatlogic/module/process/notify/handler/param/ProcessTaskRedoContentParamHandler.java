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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskRedoContentParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.PROCESS_TASK_REDO_CONTENT.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskNotifyTriggerType.REOPENPROCESSTASK)) {
            return null;
        }
        JSONObject paramObj = processTaskStepVo.getParamObj();
        if (MapUtils.isNotEmpty(paramObj)) {
            String content = paramObj.getString("content");
            content= processContent(content);
            return content;
        }
        return null;
    }
}
