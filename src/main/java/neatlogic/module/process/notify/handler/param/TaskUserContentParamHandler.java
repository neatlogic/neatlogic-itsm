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

import neatlogic.framework.dto.UrlInfoVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.HtmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class TaskUserContentParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Override
    public String getValue() {
        return ProcessTaskStepTaskNotifyParam.TASKUSERCONTENT.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
        if(stepTaskVo != null ){
            String content = stepTaskVo.getTaskStepTaskUserContent();
            if (StringUtils.isNotBlank(content)) {
                content = content.replace("<p>", "");
                content = content.replace("</p>", "");
                content = content.replace("<br>", "");
                List<UrlInfoVo> urlInfoVoList = HtmlUtil.getUrlInfoList(content, "<img src=\"", "\"");
                content = HtmlUtil.urlReplace(content, urlInfoVoList);
            }
            return content;
        }
        return null;
    }
}
