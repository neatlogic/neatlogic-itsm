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

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelIsReferencedException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
