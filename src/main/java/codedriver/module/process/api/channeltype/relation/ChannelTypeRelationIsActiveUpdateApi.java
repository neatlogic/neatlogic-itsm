package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ChannelTypeRelationIsActiveUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/isactive/update";
	}

	@Override
	public String getName() {
		return "启用或禁用服务类型关系";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活")
	})
	@Description(desc = "启用或禁用服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
	    if(channelMapper.checkChannelTypeRelationIsExists(channelTypeRelationVo.getId()) > 0) {
	        throw new ChannelTypeRelationNotFoundException(channelTypeRelationVo.getId());
	    }
	    channelMapper.updateChannelTypeRelationIsActiveById(channelTypeRelationVo);
		return null;
	}

}
