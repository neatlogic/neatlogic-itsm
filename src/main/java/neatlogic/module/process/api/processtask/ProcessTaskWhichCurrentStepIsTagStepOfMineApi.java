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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskWhichCurrentStepIsTagStepOfMineApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/currentstepistagstepofmine/list";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskwhichcurrentstepistagstepofmineapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.input.param.desc.userid"),
            @Param(name = "tag", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.input.param.desc.tag"),
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.id"),
            @Param(name = "title", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.title"),
            @Param(name = "channelName", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.channelname"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.status"),
            @Param(name = "stepName", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.stepname"),
            @Param(name = "stepStatus", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.stepstatus"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.userid"),
            @Param(name = "userName", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.username"),
            @Param(name = "teamName", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.teamname"),
            @Param(name = "roleName", type = ApiParamType.STRING, desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.output.param.desc.rolename"),
    })
    @Description(desc = "nmpap.processtaskwhichcurrentstepistagstepofmineapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskService.getProcessTaskListWhichIsProcessingByUserAndTag(jsonObj);
    }


}
