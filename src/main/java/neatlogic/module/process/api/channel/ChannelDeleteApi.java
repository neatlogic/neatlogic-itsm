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

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelIsReferencedException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelDeleteApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessTaskMapper processTaskkMapper;

    @Override
    public String getToken() {
        return "process/channel/delete";
    }

    @Override
    public String getName() {
        return "服务通道删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "服务通道uuid")
    })
    @Description(desc = "服务通道删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ChannelVo existsChannel = channelMapper.getChannelByUuid(uuid);
        if (existsChannel != null) {
            if (processTaskkMapper.getProcessTaskIdByChannelUuidLimitOne(uuid) != null) {
                throw new ChannelIsReferencedException(existsChannel.getName());
            }
            channelMapper.deleteChannelByUuid(uuid);
            channelMapper.deleteChannelProcessByChannelUuid(uuid);
            channelMapper.deleteChannelWorktimeByChannelUuid(uuid);
            channelMapper.deleteChannelUserByChannelUuid(uuid);
            channelMapper.deleteChannelPriorityByChannelUuid(uuid);
            channelMapper.deleteChannelAuthorityByChannelUuid(uuid);
            channelMapper.updateSortDecrement(existsChannel.getParentUuid(), existsChannel.getSort(), null);
        }
        return null;
    }

}
