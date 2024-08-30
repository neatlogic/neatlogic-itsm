/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
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
