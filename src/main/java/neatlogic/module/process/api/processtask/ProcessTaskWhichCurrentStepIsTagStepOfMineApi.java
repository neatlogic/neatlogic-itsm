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
        return "我的待办的工单中当前处理节点是打了某个标签的节点的工单列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "用户ID"),
            @Param(name = "tag", type = ApiParamType.STRING, isRequired = true, desc = "标签名称"),
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "工单ID"),
            @Param(name = "title", type = ApiParamType.STRING, desc = "工单标题"),
            @Param(name = "channelName", type = ApiParamType.STRING, desc = "服务名称"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "工单状态"),
            @Param(name = "stepName", type = ApiParamType.STRING, desc = "当前步骤"),
            @Param(name = "stepStatus", type = ApiParamType.STRING, desc = "当前步骤状态"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "当前步骤处理人id"),
            @Param(name = "userName", type = ApiParamType.STRING, desc = "当前步骤处理人名称"),
            @Param(name = "teamName", type = ApiParamType.STRING, desc = "当前步骤处理组"),
            @Param(name = "roleName", type = ApiParamType.STRING, desc = "当前步骤处理角色"),
    })
    @Description(desc = "我的待办的工单中当前处理节点是打了某个标签的节点的工单列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskService.getProcessTaskListWhichIsProcessingByUserAndTag(jsonObj);
    }


}
