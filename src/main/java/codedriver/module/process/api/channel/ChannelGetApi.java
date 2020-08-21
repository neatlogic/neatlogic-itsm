package codedriver.module.process.api.channel;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelGetApi extends ApiComponentBase {

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
		ChannelVo channel = channelMapper.getChannelByUuid(uuid);
		if(channel == null) {
			throw new ChannelNotFoundException(uuid);
		}
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
