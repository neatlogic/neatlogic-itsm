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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchProcessTaskListForOwnerApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private PriorityMapper priorityMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public String getName() {
        return "查询用户上报工单列表";
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "owner", type = ApiParamType.STRING, isRequired = true, desc = "上报人"),
            @Param(name = "excludeId", type = ApiParamType.LONG, isRequired = true, desc = "排除的工单ID"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessTaskVo[].class, desc = "工单列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ProcessTaskSearchVo searchVo = paramObj.toJavaObject(ProcessTaskSearchVo.class);
        int rowNum = processTaskMapper.searchProcessTaskCountByOwnerAndExcludeId(searchVo);
        if (rowNum == 0) {
            return TableResultUtil.getResult(new ArrayList(), searchVo);
        }
        searchVo.setRowNum(rowNum);
        Map<String, PriorityVo> priorityMap = new HashMap<>();
        Map<String, ChannelVo> channelMap = new HashMap<>();
        Map<String, CatalogVo> catalogMap = new HashMap<>();
        List<ProcessTaskVo> processTaskList = processTaskMapper.searchProcessTaskListByOwnerAndExcludeId(searchVo);
        Set<String> priorityUuidSet = processTaskList.stream().filter(e -> StringUtils.isNotBlank(e.getPriorityUuid())).map(ProcessTaskVo::getPriorityUuid).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(priorityUuidSet)) {
            List<PriorityVo> priorityVoList = priorityMapper.getPriorityByUuidList(new ArrayList<>(priorityUuidSet));
            priorityMap = priorityVoList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
        }
        Set<String> channelUuidSet = processTaskList.stream().filter(e -> StringUtils.isNotBlank(e.getChannelUuid())).map(ProcessTaskVo::getChannelUuid).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(channelUuidSet)) {
            List<ChannelVo> channelList = channelMapper.getChannelVoByUuidList(new ArrayList<>(channelUuidSet));
            channelMap = channelList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
            Set<String> parentUuidSet = channelList.stream().filter(e -> StringUtils.isNotBlank(e.getParentUuid())).map(ChannelVo::getParentUuid).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(parentUuidSet)) {
                List<CatalogVo> catalogList = catalogMapper.getCatalogListByUuidList(new ArrayList<>(parentUuidSet));
                catalogMap = catalogList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
            }
        }
        for (ProcessTaskVo processTaskVo : processTaskList) {
            // 优先级
            processTaskVo.setPriority(priorityMap.get(processTaskVo.getPriorityUuid()));
            // 服务目录
            ChannelVo channelVo = channelMap.get(processTaskVo.getChannelUuid());
            if (channelVo != null) {
                CatalogVo catalogVo = catalogMap.get(channelVo.getParentUuid());
                if (catalogVo != null) {
                    processTaskVo.setCatalogName(catalogVo.getName());
                }
                processTaskVo.setChannelName(channelVo.getName());
                processTaskVo.setChannelVo(channelVo);
            }
        }
        return TableResultUtil.getResult(processTaskList, searchVo);
    }

    @Override
    public String getToken() {
        return "processtask/list/forowner";
    }

    @Override
    public String getConfig() {
        return null;
    }
}
