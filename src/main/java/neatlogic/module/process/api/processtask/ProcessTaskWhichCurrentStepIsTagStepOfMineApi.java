/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskWhichCurrentStepIsTagStepOfMineApi extends PublicApiComponentBase {

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
