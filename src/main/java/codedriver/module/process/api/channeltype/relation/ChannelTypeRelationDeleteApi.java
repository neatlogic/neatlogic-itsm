package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CATALOG_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;

@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelTypeRelationDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

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
	    //TODO linbq 判断是否能删除
	    channelMapper.deleteChannelTypeRelationById(channelTypeRelationId);
        channelMapper.deleteChannelTypeRelationSourceByChannelTypeRelationId(channelTypeRelationId);
        channelMapper.deleteChannelTypeRelationTargetByChannelTypeRelationId(channelTypeRelationId);
		return null;
	}

}
