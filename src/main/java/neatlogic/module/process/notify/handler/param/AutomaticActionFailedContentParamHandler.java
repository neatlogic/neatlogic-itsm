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
