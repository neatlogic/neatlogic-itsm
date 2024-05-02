package neatlogic.module.process.api.channeltype.relation;

import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationHasReferenceException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.CATALOG_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelTypeRelationIsActiveUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

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
			@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id")

	})
	@Output({
			@Param(name = "Return", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活")
	})
	@Description(desc = "启用或禁用服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");    
	    ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationLockById(channelTypeRelationId);
	    if(channelTypeRelationVo == null) {
	        throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
	    }
		if(channelTypeMapper.checkChannelTypeRelationHasReference(channelTypeRelationId) > 0){
			throw new ChannelTypeRelationHasReferenceException(channelTypeRelationVo.getName());
		}
		channelTypeMapper.updateChannelTypeRelationIsActiveById(channelTypeRelationId);
		return 1 - channelTypeRelationVo.getIsActive();
	}

}
