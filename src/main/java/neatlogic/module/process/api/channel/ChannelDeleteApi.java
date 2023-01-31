/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.channel;

import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.exception.channel.ChannelIsReferencedException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.CATALOG_MODIFY;

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
