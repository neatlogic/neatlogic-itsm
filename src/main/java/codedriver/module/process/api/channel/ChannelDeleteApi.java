package codedriver.module.process.api.channel;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CATALOG_MODIFY;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
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
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务通道uuid")
		})
	@Description(desc = "服务通道删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		ChannelVo existsChannel = channelMapper.getChannelByUuid(uuid);
		if(existsChannel == null) {
			throw new ChannelNotFoundException(uuid);
		}
		channelMapper.deleteChannelByUuid(uuid);
		channelMapper.deleteChannelProcessByChannelUuid(uuid);
		channelMapper.deleteChannelWorktimeByChannelUuid(uuid);
		channelMapper.deleteChannelUserByChannelUuid(uuid);
		channelMapper.deleteChannelPriorityByChannelUuid(uuid);
		channelMapper.deleteChannelAuthorityByChannelUuid(uuid);
		channelMapper.updateSortDecrement(existsChannel.getParentUuid(), existsChannel.getSort(), null);
		return null;
	}

}
