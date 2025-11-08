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
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

@Component
public class AutomaticActionFailedContentParamHandler extends ProcessTaskNotifyParamHandlerBase {
    @Override
    public String getValue() {
        return ProcessTaskStepAutomaticNotifyParam.ACTION_FAILED_CONTENT.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskStepAutomaticNotifyTriggerType.ACTION_FAILED)) {
            return null;
        }
        if (processTaskStepVo == null) {
            return null;
        }
        JSONObject paramObj = processTaskStepVo.getParamObj();
        if (MapUtils.isEmpty(paramObj)) {
            return null;
        }
        String content = paramObj.getString("actionFailedContent");
        content= processContent(content);
        return content;
    }
}
