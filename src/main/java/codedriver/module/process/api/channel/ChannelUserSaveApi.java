package codedriver.module.process.api.channel;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ChannelUserSaveApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "process/channel/user/save";
	}

	@Override
	public String getName() {
		return "服务通道收藏控制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务通道uuid"),
		@Param(name = "action", type = ApiParamType.ENUM, isRequired = true, desc = "1:收藏，0：取消", rule = "0,1")
		})
	@Description(desc = "服务通道收藏控制接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String channelUuid = jsonObj.getString("channelUuid");
		if(channelMapper.checkChannelIsExists(channelUuid) == 0) {
			throw new ChannelNotFoundException(channelUuid);
		}
		int action = jsonObj.getIntValue("action");
		String userUuid = UserContext.get().getUserUuid(true);
		if(action == 1) {
			channelMapper.replaceChannelUser(userUuid, channelUuid);
		}else {
			channelMapper.deleteChannelUser(userUuid, channelUuid);
		}
		return null;
	}

}
