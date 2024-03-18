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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.user.AgentIsUserSelfException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskAgentMapper;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentCompobVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentInfoVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAgentSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public String getToken() {
        return "processtask/agent/save";
    }

    @Override
    public String getName() {
        return "保存用户任务授权信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "beginTime", type = ApiParamType.LONG, isRequired = true, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, isRequired = true, desc = "结束时间"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "启用"),
            @Param(name = "compobList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "授权对象列表")
    })
    @Output({})
    @Description(desc = "保存用户任务授权信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskAgentInfoVo processTaskAgentInfoVo = JSONObject.toJavaObject(jsonObj, ProcessTaskAgentInfoVo.class);
        List<ProcessTaskAgentCompobVo> compobList = processTaskAgentInfoVo.getCompobList();
        if (CollectionUtils.isEmpty(compobList)) {
            throw new ParamIrregularException("compobList");
        }
        String fromUserUuid = UserContext.get().getUserUuid(true);
        List<Long> processTaskAgentIdList = processTaskAgentMapper.getProcessTaskAgentIdListByFromUserUuid(fromUserUuid);
        if (CollectionUtils.isNotEmpty(processTaskAgentIdList)) {
            processTaskAgentMapper.deleteProcessTaskAgentByFromUserUuid(fromUserUuid);
            processTaskAgentMapper.deleteProcessTaskAgentTargetByProcessTaskAgentIdList(processTaskAgentIdList);
        }

        Map<String, Long> toUserUuidProcessTaskAgentIdMap = new HashMap<>();
        ProcessTaskAgentVo processTaskAgentVo = new ProcessTaskAgentVo();
        processTaskAgentVo.setBeginTime(processTaskAgentInfoVo.getBeginTime());
        processTaskAgentVo.setEndTime(processTaskAgentInfoVo.getEndTime());
        processTaskAgentVo.setIsActive(processTaskAgentInfoVo.getIsActive());
        processTaskAgentVo.setFromUserUuid(fromUserUuid);
        for (ProcessTaskAgentCompobVo compobVo : compobList) {
            String toUserUuid = compobVo.getToUserUuid();
            if (toUserUuid.contains(GroupSearch.USER.getValuePlugin())) {
                toUserUuid = toUserUuid.substring(5);
            }
            Long processTaskAgentId = toUserUuidProcessTaskAgentIdMap.get(toUserUuid);
            if (processTaskAgentId == null) {
                if (fromUserUuid.equals(toUserUuid)) {
                    throw new AgentIsUserSelfException();
                }
                if (userMapper.checkUserIsExists(toUserUuid) == 0) {
                    throw new UserNotFoundException(toUserUuid);
                }
                processTaskAgentVo.setId(null);
                processTaskAgentVo.setToUserUuid(toUserUuid);
                processTaskAgentMapper.insertProcessTaskAgent(processTaskAgentVo);
                processTaskAgentId = processTaskAgentVo.getId();
                toUserUuidProcessTaskAgentIdMap.put(toUserUuid, processTaskAgentId);
            }
            List<ProcessTaskAgentTargetVo> targetList = compobVo.getTargetList();
            for (ProcessTaskAgentTargetVo target : targetList) {
                if ("channel".equals(target.getType())) {
                    if (channelMapper.checkChannelIsExists(target.getTarget()) == 0) {
                        throw new ChannelNotFoundException(target.getTarget());
                    }
                } else if ("catalog".equals(target.getType())) {
                    if (catalogMapper.checkCatalogIsExists(target.getTarget()) == 0) {
                        throw new CatalogNotFoundException(target.getTarget());
                    }
                }
                target.setProcessTaskAgentId(processTaskAgentId);
                processTaskAgentMapper.insertIgnoreProcessTaskAgentTarget(target);
            }
        }
        return null;
    }

}
