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

package neatlogic.module.process.api.processtask.task;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskCompleteApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/complete";
    }

    @Override
    public String getName() {
        return "任务完成接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "任务id"),
            @Param(name = "button", type = ApiParamType.STRING, desc = "按钮"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "描述"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Output({})
    @Description(desc = "任务完成接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        String content = jsonObj.getString("content");
        String button = jsonObj.getString("button");
        String source = jsonObj.getString("source");
        return processTaskStepTaskService.completeTask(id, content, button, source);
    }
}
