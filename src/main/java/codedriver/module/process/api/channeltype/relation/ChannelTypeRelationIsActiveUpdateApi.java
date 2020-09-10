package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
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
		@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活")
	})
	@Description(desc = "启用或禁用服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");    
	    ChannelTypeRelationVo channelTypeRelationVo = channelMapper.getChannelTypeRelationById(channelTypeRelationId);
	    if(channelTypeRelationVo == null) {
	        throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
	    }
	    Integer isActive = jsonObj.getInteger("isActive");
	    if(Objects.equal(isActive, channelTypeRelationVo.getIsActive())) {    
	        return null;
	    }
	    channelTypeRelationVo.setIsActive(isActive);
	    channelMapper.updateChannelTypeRelationById(channelTypeRelationVo);
		return null;
	}

}