package codedriver.module.process.api.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelVo;
@Service
public class ChannelUserSaveApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "channel/user/save";
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
		ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
		if(channel == null) {
			throw new ChannelNotFoundException(channelUuid);
		}
		int action = jsonObj.getIntValue("action");
		String userId = UserContext.get().getUserId();
		if(action == 1) {
			channelMapper.replaceChannelUser(userId, channelUuid);
		}else {
			channelMapper.deleteChannelUser(userId, channelUuid);
		}
		return null;
	}

}
