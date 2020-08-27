package codedriver.module.process.api.event;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeAuthGetApi extends PrivateApiComponentBase{

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public String getToken() {
		return "eventtype/auth/get";
	}

	@Override
	public String getName() {
		return "获取事件类型授权";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID",isRequired = true)
	})
    @Output({
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "当前事件类型自身的授权"),
            @Param(name = "childrenAuthList", type = ApiParamType.JSONARRAY, desc = "当前事件类型子类的授权"),
    })
	@Description(desc = "获取事件类型授权")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		JSONObject result = new JSONObject();

		Long id = jsonObj.getLong("id");
		EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(id);
		if(eventTypeVo == null){
			throw new EventTypeNotFoundException(id);
		}
		/** 获取当前事件类型本身的授权 */
		List<AuthorityVo> authList = eventTypeMapper.getAuthorityByEventTypeId(id);
		EventTypeVo authVo = new EventTypeVo();
		authVo.setAuthorityVoList(authList);
		result.put("authList",authVo.getAuthorityList());
		/** 获取子类的授权 */
		List<EventTypeVo> children = eventTypeMapper.getChildrenByLeftRightCode(eventTypeVo.getLft(), eventTypeVo.getRht());
		if(CollectionUtils.isNotEmpty(children)){
			List<Long> childrenIds = children.stream().map(EventTypeVo::getId).collect(Collectors.toList());
			/** 不同的子类可能有相同的权限，用set去重 */
			Set<AuthorityVo> childrenAuthSet = new HashSet<>();
			for(Long childId : childrenIds){
				List<AuthorityVo> childrenAuths = eventTypeMapper.getAuthorityByEventTypeId(childId);
				if(CollectionUtils.isNotEmpty(childrenAuths)){
					for(AuthorityVo vo : childrenAuths){
						childrenAuthSet.add(vo);
					}
				}
			}
			if(CollectionUtils.isNotEmpty(childrenAuthSet)){
				List<AuthorityVo> childrenAuthList = childrenAuthSet.stream().collect(Collectors.toList());
				EventTypeVo childrenAuthVo = new EventTypeVo();
				childrenAuthVo.setAuthorityVoList(childrenAuthList);
				result.put("childrenAuthList",childrenAuthVo.getAuthorityList());
			}
		}

		return result;
	}
}
