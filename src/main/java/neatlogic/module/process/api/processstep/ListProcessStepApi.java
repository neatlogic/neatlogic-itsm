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
        return "nmpap.listprocessstepapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "nmpap.listprocessstepapi.input.param.desc.channeluuid"),
            @Param(name = "processUuid", type = ApiParamType.STRING, desc = "nmpap.listprocessstepapi.input.param.desc.processuuid"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "nmpap.listprocessstepapi.input.param.desc.defaultvalue")
    })
    @Output({
            @Param(explode = ProcessStepVo[].class, desc = "nmpap.listprocessstepapi.output.param.desc.processstepvo")
    })
    @Description(desc = "nmpap.listprocessstepapi.getname")
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
