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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskCompleteApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/complete";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskcompleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskcompleteapi.input.param.desc.processtaskstepid"),
            @Param(name = "nextStepId", type = ApiParamType.LONG, desc = "nmpap.processtaskcompleteapi.input.param.desc.nextstepid"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", isRequired = true, desc = "nmpap.processtaskcompleteapi.input.param.desc.action"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "nmpap.processtaskcompleteapi.input.param.desc.content"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskcompleteapi.input.param.desc.source"),
            @Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "nmpap.processtaskcompleteapi.input.param.desc.assignworkerlist")
    })
    @Description(desc = "nmpap.processtaskcompleteapi.getname")
    @Override
    @ResubmitInterval(3)
    public Object myDoService(JSONObject jsonObj) throws Exception {
        processTaskService.completeProcessTaskStep(jsonObj);
        return null;
    }

}
