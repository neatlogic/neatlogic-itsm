package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationGetApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

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
		ChannelTypeRelationVo channelTypeRelationVo = channelMapper.getChannelTypeRelationById(channelTypeRelationId);
		if(channelTypeRelationVo == null) {
		    throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
		}
		List<String> sourceList = channelMapper.getChannelTypeRelationSourceListByChannelTypeRelationId(channelTypeRelationId);
		channelTypeRelationVo.setSourceList(sourceList);
		List<String> targetList = channelMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
		channelTypeRelationVo.setTargetList(targetList);
		return channelTypeRelationVo;
	}

}
