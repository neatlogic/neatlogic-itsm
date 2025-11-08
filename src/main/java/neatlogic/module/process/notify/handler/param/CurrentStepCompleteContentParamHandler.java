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
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskStepNotifyTriggerType.SUCCEED)) {
            return null;
        }
        // 查询步骤的所有处理内容，已倒序排好
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepVo.getId());
        // 遍历列表，找出最近一次处理内容
        for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
            if (Objects.equals(contentVo.getType(), ProcessTaskStepOperationType.STEP_COMPLETE.getValue())) {
                String contentHash = contentVo.getContentHash();
                if (StringUtils.isBlank(contentHash)) {
                    return null;
                }
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
                content= processContent(content);
                return content;
            }
        }
        return null;
    }
}
