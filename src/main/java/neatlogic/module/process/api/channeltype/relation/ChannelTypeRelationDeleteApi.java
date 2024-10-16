package neatlogic.module.process.api.channeltype.relation;

import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationHasReferenceException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
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

@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelTypeRelationDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/delete";
	}

	@Override
	public String getName() {
		return "删除服务类型关系";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id")
	})
	@Description(desc = "删除服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
		ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationLockById(channelTypeRelationId);
		if(channelTypeRelationVo == null) {
			throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
		}
	    /** 判断是否能删除，被服务引用则不能删除 **/
		if(channelTypeMapper.checkChannelTypeRelationHasReference(channelTypeRelationId) > 0){
			throw new ChannelTypeRelationHasReferenceException(channelTypeRelationVo.getName());
		}
		/** 判断是逻辑删除还是物理删除，如果被工单使用，则逻辑删除，否则物理删除。 **/
		if(channelTypeMapper.checkChannelTypeRelationIsUsedByChannelTypeRelationId(channelTypeRelationId) != null){
			channelTypeMapper.updateChannelTypeRelationToDeleteById(channelTypeRelationId);
		}else{
			channelTypeMapper.deleteChannelTypeRelationById(channelTypeRelationId);
			channelTypeMapper.deleteChannelTypeRelationSourceByChannelTypeRelationId(channelTypeRelationId);
			channelTypeMapper.deleteChannelTypeRelationTargetByChannelTypeRelationId(channelTypeRelationId);
		}
		return null;
	}

}
