package neatlogic.module.process.api.channeltype.relation;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationGetApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/get";
	}

	@Override
	public String getName() {
		return "查询服务类型关系";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id")
	})
	@Output({
		@Param(name = "Return", explode = ChannelTypeRelationVo.class, desc = "服务类型关系信息")
	})
	@Description(desc = "查询服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
		ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationById(channelTypeRelationId);
		if(channelTypeRelationVo == null) {
		    throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
		}
		List<String> sourceList = channelTypeMapper.getChannelTypeRelationSourceListByChannelTypeRelationId(channelTypeRelationId);
		channelTypeRelationVo.setSourceList(sourceList);
		List<String> targetList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
		channelTypeRelationVo.setTargetList(targetList);
		return channelTypeRelationVo;
	}

}
