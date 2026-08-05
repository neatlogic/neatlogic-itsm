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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskStepBatchCompleteApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/batch/complete";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskstepbatchcompleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpap.processtaskstepbatchcompleteapi.input.param.desc.processtaskidlist"),
            @Param(name = "tag", type = ApiParamType.STRING, desc = "nmpap.processtaskstepbatchcompleteapi.input.param.desc.tag"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "nmpap.processtaskstepbatchcompleteapi.input.param.desc.content"),
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtaskstepbatchcompleteapi.input.param.desc.userid"),
    })
    @Description(desc = "nmpap.processtaskstepbatchcompleteapi.getname")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskService.batchCompleteProcessTaskStep(jsonObj);
    }

}
