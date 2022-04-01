package codedriver.module.process.api.channel;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelGetApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channel/get";
	}

	@Override
	public String getName() {
		return "服务通道获取信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务通道uuid")
		})
	@Output({
		@Param(explode=ChannelVo.class,desc="服务通道信息")
	})
	@Description(desc = "服务通道获取信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		ChannelVo channelVo = channelMapper.getChannelByUuid(uuid);
		if(channelVo == null) {
			throw new ChannelNotFoundException(uuid);
		}
		ChannelVo channel = new ChannelVo(channelVo);
		String processUuid = channelMapper.getProcessUuidByChannelUuid(uuid);
		channel.setProcessUuid(processUuid);
		String worktimeUuid = channelMapper.getWorktimeUuidByChannelUuid(uuid);
		channel.setWorktimeUuid(worktimeUuid);
		List<String> priorityUuidList = new ArrayList<>();
		List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(uuid);
		for(ChannelPriorityVo channelPriority : channelPriorityList) {
			priorityUuidList.add(channelPriority.getPriorityUuid());
			if(channelPriority.getIsDefault().intValue() == 1) {
				channel.setDefaultPriorityUuid(channelPriority.getPriorityUuid());
			}
		}
		channel.setPriorityUuidList(priorityUuidList);
		List<AuthorityVo> authorityVoList = channelMapper.getChannelAuthorityListByChannelUuid(uuid);
		channel.setAuthorityVoList(authorityVoList);
		if(channelMapper.checkChannelIsFavorite(UserContext.get().getUserUuid(true), uuid) == 0) {
		    channel.setIsFavorite(0);
		}else {
		    channel.setIsFavorite(1);
		}
		return channel;
	}

}
