package neatlogic.module.process.api.channeltype.relation;

import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationHasReferenceException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.CATALOG_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNameRepeatException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelTypeRelationSaveApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/save";
	}

	@Override
	public String getName() {
		return "nmpacr.channeltyperelationsaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, desc = "nmpacr.channeltyperelationsaveapi.input.param.desc.id"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "nmpacr.channeltyperelationsaveapi.input.param.desc.name"),
        @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "nmpacr.channeltyperelationsaveapi.input.param.desc.isactive"),
		@Param(name = "sourceList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpacr.channeltyperelationsaveapi.input.param.desc.sourcelist"),
		@Param(name = "targetList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpacr.channeltyperelationsaveapi.input.param.desc.targetlist")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.LONG, desc = "nmpacr.channeltyperelationsaveapi.output.param.desc.return.name")
	})
	@Description(desc = "nmpacr.channeltyperelationsaveapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
	    if(channelTypeMapper.checkChannelTypeRelationNameIsRepeat(channelTypeRelationVo) > 0) {
	        throw new ChannelTypeRelationNameRepeatException(channelTypeRelationVo.getName());
	    }
	    Long id = jsonObj.getLong("id");
	    if(id == null) {
	        channelTypeMapper.insertChannelTypeRelation(channelTypeRelationVo);
	    }else {
			ChannelTypeRelationVo oldChannelTypeRelationVo = channelTypeMapper.getChannelTypeRelationLockById(id);
	        if(oldChannelTypeRelationVo == null) {
	            throw new ChannelTypeRelationNotFoundException(id);
	        }
			if(channelTypeMapper.checkChannelTypeRelationHasReference(id) > 0){
				throw new ChannelTypeRelationHasReferenceException(oldChannelTypeRelationVo.getName());
			}
	        channelTypeMapper.updateChannelTypeRelationById(channelTypeRelationVo);
	        channelTypeMapper.deleteChannelTypeRelationSourceByChannelTypeRelationId(id);
	        channelTypeMapper.deleteChannelTypeRelationTargetByChannelTypeRelationId(id);
	    }
	    for(String source : channelTypeRelationVo.getSourceList()) {
	        channelTypeMapper.insertChannelTypeRelationSource(channelTypeRelationVo.getId(), source);
	    }
	    for(String target : channelTypeRelationVo.getTargetList()) {
            channelTypeMapper.insertChannelTypeRelationTarget(channelTypeRelationVo.getId(), target);
        }
		return channelTypeRelationVo.getId();
	}

	public IValid name(){
		return value -> {
			ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(value, ChannelTypeRelationVo.class);
			if(channelTypeMapper.checkChannelTypeRelationNameIsRepeat(channelTypeRelationVo) > 0) {
				return new FieldValidResultVo(new ChannelTypeRelationNameRepeatException(channelTypeRelationVo.getName()));
			}
			return new FieldValidResultVo();
		};
	}

}
