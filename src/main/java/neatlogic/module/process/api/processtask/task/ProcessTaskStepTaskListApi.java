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
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2022/04/08 12:18
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskListApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskMapper processTaskMapper;

    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/list";
    }

    @Override
    public String getName() {
        return "获取工单步骤任务列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskStepId", isRequired = true, type = ApiParamType.LONG, desc = "工单步骤id")
    })
    @Output({
            @Param(name = "Return", explode = TaskConfigVo[].class, desc = "任务列表")
    })
    @Description(desc = "获取工单步骤任务列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
        }
        if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStepStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
            return processTaskStepTaskService.getTaskConfigList(processTaskStepVo);
        }
        return null;
    }
}
