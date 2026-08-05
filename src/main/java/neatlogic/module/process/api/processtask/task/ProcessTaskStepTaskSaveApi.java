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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskSaveApi extends PrivateApiComponentBase {

    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/save";
    }

    @Override
    public String getName() {
        return "nmpapt.processtasksteptasksaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.processtaskstepid"),
            @Param(name = "stepTaskUserVoList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.steptaskuservolist"),
            @Param(name = "taskConfigId", type = ApiParamType.LONG, isRequired = true, desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.taskconfigid"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.content"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpapt.processtasksteptasksaveapi.input.param.desc.source")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.LONG, desc = "nmpapt.processtasksteptasksaveapi.output.param.desc.return.name")
    })
    @Description(desc = "nmpapt.processtasksteptasksaveapi.getname")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        String source = jsonObj.getString("source");
        JSONArray stepTaskUserVoList = jsonObj.getJSONArray("stepTaskUserVoList");
        if (CollectionUtils.isEmpty(stepTaskUserVoList)) {
            throw new ParamIrregularException("stepTaskUserVoList");
        }
        ProcessTaskStepTaskVo processTaskStepTaskVo = jsonObj.toJavaObject(ProcessTaskStepTaskVo.class);
        return processTaskStepTaskService.saveTask(id, processTaskStepTaskVo, stepTaskUserVoList, source);
    }
}
