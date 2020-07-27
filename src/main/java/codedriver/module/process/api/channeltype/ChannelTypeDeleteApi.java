package codedriver.module.process.api.channeltype;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ChannelTypeDeleteApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/delete";
	}

	@Override
	public String getName() {
		return "服务类型信息删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "服务类型uuid")
	})
	@Output({})
	@Description(desc = "服务类型信息删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(channelMapper.checkChannelTypeIsExists(uuid) == 0) {
			throw new ChannelTypeNotFoundException(uuid);
		}
		channelMapper.deleteChannelTypeByUuid(uuid);
		return null;
	}

}
