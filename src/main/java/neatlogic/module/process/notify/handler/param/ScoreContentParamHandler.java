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

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.score.ProcessTaskScoreMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScoreContentParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskScoreMapper processTaskScoreMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.PROCESS_TASK_SCORE_CONTENT.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskNotifyTriggerType.SCOREPROCESSTASK)) {
            return null;
        }
        String contentHash = processTaskScoreMapper.getProcessTaskScoreContentHashByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (StringUtils.isBlank(contentHash)) {
            return null;
        }
        String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
        content= processContent(content);
        return content;
    }
}
