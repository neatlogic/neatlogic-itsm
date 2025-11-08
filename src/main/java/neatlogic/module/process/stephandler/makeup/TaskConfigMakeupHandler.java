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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepTaskConfigVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class TaskConfigMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getName() {
        return "taskConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        //保存子任务
        JSONObject taskConfig = stepConfigObj.getJSONObject("taskConfig");
        if (MapUtils.isNotEmpty(taskConfig)) {
            ProcessStepTaskConfigVo taskConfigVo = JSON.toJavaObject(taskConfig, ProcessStepTaskConfigVo.class);
            if (CollectionUtils.isNotEmpty(taskConfigVo.getIdList())) {
                if (Objects.equals(action, "save")) {
                    taskConfigVo.getIdList().forEach(id -> {
                        ProcessStepTaskConfigVo tmpVo = new ProcessStepTaskConfigVo(processStepVo.getUuid(), id);
                        processMapper.insertProcessStepTask(tmpVo);
                    });
                } else if (Objects.equals(action, "delete")) {
                    processMapper.deleteProcessStepTaskByProcessStepUuid(processStepVo.getUuid());
                }
            }
        }
    }
}
