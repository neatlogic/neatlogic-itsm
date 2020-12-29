package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CATALOG_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNameRepeatException;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelTypeRelationSaveApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/save";
	}

	@Override
	public String getName() {
		return "添加服务类型关系";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, desc = "服务类型关系id"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "名称"),
        @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活"),
		@Param(name = "sourceList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "来源服务类型uuid列表"),
		@Param(name = "targetList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "目标服务类型uuid列表")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.LONG, desc = "服务类型关系id")
	})
	@Description(desc = "添加服务类型关系")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
	    if(channelMapper.checkChannelTypeRelationNameIsRepeat(channelTypeRelationVo) > 0) {
	        throw new ChannelTypeRelationNameRepeatException(channelTypeRelationVo.getName());
	    }
	    Long id = jsonObj.getLong("id");
	    if(id == null) {
	        channelMapper.insertChannelTypeRelation(channelTypeRelationVo);
	    }else {
	        if(channelMapper.checkChannelTypeRelationIsExists(id) == 0) {
	            throw new ChannelTypeRelationNotFoundException(id);
	        }
	        channelMapper.updateChannelTypeRelationById(channelTypeRelationVo);
	        channelMapper.deleteChannelTypeRelationSourceByChannelTypeRelationId(id);
	        channelMapper.deleteChannelTypeRelationTargetByChannelTypeRelationId(id);
	    }
	    for(String source : channelTypeRelationVo.getSourceList()) {
	        channelMapper.insertChannelTypeRelationSource(channelTypeRelationVo.getId(), source);	        
	    }
	    for(String target : channelTypeRelationVo.getTargetList()) {
            channelMapper.insertChannelTypeRelationTarget(channelTypeRelationVo.getId(), target);           
        }
		return channelTypeRelationVo.getId();
	}

}
