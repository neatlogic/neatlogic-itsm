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

import neatlogic.framework.dto.UrlInfoVo;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.HtmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class CurrentStepCompleteContentParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.PROCESS_TASK_CURRENT_STEP_COMPLETE_CONTENT.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        // 查询步骤的所有处理内容，已倒序排好
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepVo.getId());
        // 遍历列表，找出最近一次处理内容
        for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
            if (Objects.equals(contentVo.getType(), ProcessTaskOperationType.STEP_COMPLETE.getValue())) {
                String contentHash = contentVo.getContentHash();
                if (StringUtils.isBlank(contentHash)) {
                    return null;
                }
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
                if (StringUtils.isNotBlank(content)) {
                    content = content.replace("<p>", "");
                    content = content.replace("</p>", "");
                    content = content.replace("<br>", "");
                    List<UrlInfoVo> urlInfoVoList = HtmlUtil.getUrlInfoList(content, "<img src=\"", "\"");
                    content = HtmlUtil.urlReplace(content, urlInfoVoList);
                }
                return content;
            }
        }
        return null;
    }
}
