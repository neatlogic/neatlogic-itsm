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

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class CommentTemplateIdMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getName() {
        return "commentTemplateId";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        //保存回复模版ID
        Long commentTemplateId = stepConfigObj.getLong("commentTemplateId");
        if (commentTemplateId != null) {
            if (Objects.equals(action, "save")) {
                processMapper.insertProcessStepCommentTemplate(processStepVo.getUuid(), commentTemplateId);
            } else if (Objects.equals(action, "delete")) {
                processMapper.deleteProcessStepCommentTemplate(processStepVo.getUuid());
            }
        }
    }
}
