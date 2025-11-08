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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
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
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
            @Param(name = "nextStepId", type = ApiParamType.LONG, desc = "激活下一步骤Id（如果有且仅有一个下一节点，则可以不传这个参数）"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", isRequired = true, desc = "操作类型"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "原因"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
            @Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "分配步骤处理人信息列表，格式[{\"processTaskStepId\":1, \"workerList\":[\"user#xxx\",\"team#xxx\",\"role#xxx\"]}]")
    })
    @Description(desc = "nmpap.processtaskcompleteapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        processTaskService.completeProcessTaskStep(jsonObj);
        return null;
    }

}
