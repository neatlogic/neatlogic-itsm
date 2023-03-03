/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.api.processtask.agent;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskAgentMapper;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentCompobVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentInfoVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskAgentGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;

    @Override
    public String getToken() {
        return "processtask/agent/get";
    }

    @Override
    public String getName() {
        return "获取用户任务授权信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(explode = ProcessTaskAgentInfoVo.class, desc = "任务授权信息")
    })
    @Description(desc = "获取用户任务授权信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String fromUserUuid = UserContext.get().getUserUuid(true);
        List<ProcessTaskAgentVo> processTaskAgentList = processTaskAgentMapper.getProcessTaskAgentListByFromUserUuid(fromUserUuid);
        if (CollectionUtils.isNotEmpty(processTaskAgentList)) {
            List<ProcessTaskAgentCompobVo> combopList = new ArrayList<>();
            for (ProcessTaskAgentVo processTaskAgentVo : processTaskAgentList) {
//                List<String> targetList = new ArrayList<>();
                List<ProcessTaskAgentTargetVo> processTaskAgentTargetList = processTaskAgentMapper.getProcessTaskAgentTargetListByProcessTaskAgentId(processTaskAgentVo.getId());
//                for (ProcessTaskAgentTargetVo processTaskAgentTargetVo : processTaskAgentTargetList) {
//                    targetList.add(processTaskAgentTargetVo.getType() + "#" + processTaskAgentTargetVo.getTarget());
//                }
                ProcessTaskAgentCompobVo processTaskAgentCompobVo = new ProcessTaskAgentCompobVo();
                processTaskAgentCompobVo.setToUserUuid(GroupSearch.USER.getValuePlugin() + processTaskAgentVo.getToUserUuid());
                processTaskAgentCompobVo.setTargetList(processTaskAgentTargetList);
                combopList.add(processTaskAgentCompobVo);
            }
            ProcessTaskAgentVo processTaskAgentVo = processTaskAgentList.get(0);
            ProcessTaskAgentInfoVo processTaskAgentInfoVo = new ProcessTaskAgentInfoVo();
            processTaskAgentInfoVo.setBeginTime(processTaskAgentVo.getBeginTime());
            processTaskAgentInfoVo.setEndTime(processTaskAgentVo.getEndTime());
            processTaskAgentInfoVo.setIsActive(processTaskAgentVo.getIsActive());
            processTaskAgentInfoVo.setCompobList(combopList);
            return processTaskAgentInfoVo;
        }
        return null;
    }
}
