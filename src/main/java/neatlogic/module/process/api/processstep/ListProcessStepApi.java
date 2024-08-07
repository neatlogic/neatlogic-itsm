/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.api.processstep;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListProcessStepApi extends PrivateApiComponentBase {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "process/step/list";
    }

    @Override
    public String getName() {
        return "获取流程步骤列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务uuid，优先使用服务内定义的流程"),
            @Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "选中值")
    })
    @Output({
            @Param(explode = ProcessStepVo[].class, desc = "流程节点列表")
    })
    @Description(desc = "流程节点组件检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
        if(CollectionUtils.isNotEmpty(defaultValue)){
            return processMapper.getProcessStepListByUuidList(defaultValue.toJavaList(String.class));
        }
        String channelUuid = jsonObj.getString("channelUuid");
        String processUuid = jsonObj.getString("processUuid");
        if (StringUtils.isNotBlank(channelUuid)) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channelUuid);
            }
            processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        }
        if (StringUtils.isBlank(processUuid)) {
            throw new ParamIrregularException("processUuid");
        }
        return processMapper.getProcessStepDetailByProcessUuidAndType(processUuid, ProcessStepType.PROCESS.getValue());
    }
}
