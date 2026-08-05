package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ChannelUserSaveApi extends PrivateApiComponentBase {

	@Resource
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "process/channel/user/save";
	}

	@Override
	public String getName() {
		return "nmpac.channelusersaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channelusersaveapi.input.param.desc.channeluuid"),
		@Param(name = "action", type = ApiParamType.ENUM, isRequired = true, desc = "nmpac.channelusersaveapi.input.param.desc.action", rule = "0,1")
		})
	@Description(desc = "nmpac.channelusersaveapi.getname")
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
