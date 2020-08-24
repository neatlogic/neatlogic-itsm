package codedriver.module.process.api.event;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.EventTypeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@AuthAction(name = "EVENT_TYPE_MODIFY")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventTypeSaveApi extends ApiComponentBase{

	@Autowired
	private EventTypeService eventTypeService;

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public String getToken() {
		return "eventtype/save";
	}

	@Override
	public String getName() {
		return "保存事件类型信息";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "事件类型名称",isRequired=true, xss=true),
			@Param(name = "parentId", type = ApiParamType.LONG, desc = "父类型id"),
			@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
	})
	@Output({@Param(name = "eventTypeId", type = ApiParamType.LONG, desc = "保存的事件类型ID")})
	@Description(desc = "保存事件类型信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		Long id = jsonObj.getLong("id");
		EventTypeVo eventType = new EventTypeVo();
		eventType.setName(jsonObj.getString("name"));
		if(id != null){
			if(eventTypeMapper.checkEventTypeIsExists(id) == 0){
				throw new EventTypeNotFoundException(id);
			}
			eventType.setId(id);
			eventTypeMapper.updateEventTypeNameById(eventType);
		}else{
			eventTypeMapper.getEventTypeCountOnLock();
			if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
				eventTypeService.rebuildLeftRightCode();
			}
			Long parentId = jsonObj.getLong("parentId");
			if (parentId == null){
				parentId = EventTypeVo.ROOT_ID;
			}
			EventTypeVo parentEventType;
			if(EventTypeVo.ROOT_ID.equals(parentId)){
				parentEventType = eventTypeService.buildRootEventType();
			}else{
				parentEventType = eventTypeMapper.getEventTypeById(parentId);
				if(parentEventType == null) {
					throw new EventTypeNotFoundException(parentId);
				}
			}
			eventType.setParentId(parentId);
			eventType.setLft(parentEventType.getRht());
			eventType.setRht(eventType.getLft() + 1);
			//更新插入位置右边的左右编码值
			eventTypeMapper.batchUpdateEventTypeLeftCode(eventType.getLft(), 2);
			eventTypeMapper.batchUpdateEventTypeRightCode(eventType.getLft(), 2);

			eventTypeMapper.insertEventType(eventType);

			/** 保存授权信息 */
			JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
			if(CollectionUtils.isNotEmpty(authorityArray)){
				List<String> authorityList = authorityArray.toJavaList(String.class);
				eventType.setAuthorityList(authorityList);
				List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
				if(CollectionUtils.isNotEmpty(authorityVoList)){
					for(AuthorityVo authorityVo : authorityVoList) {
						eventTypeMapper.insertEventTypeAuthority(authorityVo,eventType.getId());
					}
				}
			}
		}
		returnObj.put("eventTypeId",eventType.getId());
		return returnObj;
	}
}
