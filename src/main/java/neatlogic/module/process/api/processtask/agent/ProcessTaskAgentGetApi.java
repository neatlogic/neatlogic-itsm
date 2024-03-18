/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.processtask.agent;

import com.alibaba.fastjson.JSONObject;
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
