package codedriver.module.process.api.event;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeSearchApi extends ApiComponentBase {

    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/tree/search";
    }

    @Override
    public String getName() {
        return "检索事件类型架构";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "id", type = ApiParamType.LONG, xss = true, desc = "主键ID"),
    	@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字")
    })
    @Output({
    	@Param(name = "children", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class,desc = "事件类型架构集合")
    })
    @Description(desc = "检索事件类型架构")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();
		resultObj.put("children", new ArrayList<>());
		List<EventTypeVo> eventTypeList = new ArrayList<>();
		Long id = jsonObj.getLong("id");
		String keyword = jsonObj.getString("keyword");
		List<Long> eventTypeIdList = new ArrayList<>();
		Map<Long, EventTypeVo> eventTypeMap = new HashMap<>();
		if(id != null){
			EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(id);
			if(eventTypeVo == null) {
				throw new EventTypeNotFoundException(id);
			}
			eventTypeList = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
			for(EventTypeVo eventType : eventTypeList) {
				eventTypeMap.put(eventType.getId(), eventType);
				eventTypeIdList.add(eventType.getId());
			}
		}else if(StringUtils.isNotBlank(keyword)){
			EventTypeVo keywordEventType = new EventTypeVo();
			keywordEventType.setKeyword(keyword);
			List<EventTypeVo> targetEventTypeList = eventTypeMapper.searchEventType(keywordEventType);
			for(EventTypeVo eventTypeVo : targetEventTypeList) {
				List<EventTypeVo> ancestorsAndSelf = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
				for(EventTypeVo eventType : ancestorsAndSelf) {
					if(!eventTypeIdList.contains(eventType.getId())) {
						eventTypeMap.put(eventType.getId(), eventType);
						eventTypeIdList.add(eventType.getId());
						eventTypeList.add(eventType);
					}
				}
			}
		}else {
			return resultObj;
		}

		if(CollectionUtils.isNotEmpty(eventTypeList)) {
			EventTypeVo rootEventType = new EventTypeVo();
			rootEventType.setId(EventTypeVo.ROOT_ID);
			rootEventType.setName("root");
			rootEventType.setParentId(EventTypeVo.ROOT_PARENTID);
			eventTypeMap.put(EventTypeVo.ROOT_ID, rootEventType);
			List<EventTypeVo> eventTypeSolutionCountAndChildCountList = eventTypeMapper.getEventTypeSolutionCountAndChildCountListByIdList(eventTypeIdList);
			Map<Long, EventTypeVo> eventTypeSolutionCountAndChildCountMap = new HashMap<>();
			for(EventTypeVo eventType : eventTypeSolutionCountAndChildCountList) {
				eventTypeSolutionCountAndChildCountMap.put(eventType.getId(), eventType);
			}
			for(EventTypeVo eventType : eventTypeList) {
				EventTypeVo parentEventType = eventTypeMap.get(eventType.getParentId());
				if(parentEventType != null) {
					eventType.setParent(parentEventType);
				}
				EventTypeVo eventTypeSolutionCountAndChildCount = eventTypeSolutionCountAndChildCountMap.get(eventType.getId());
				if(eventTypeSolutionCountAndChildCount != null) {
					eventType.setChildCount(eventTypeSolutionCountAndChildCount.getChildCount());
					eventType.setSolutionCount(eventTypeSolutionCountAndChildCount.getSolutionCount());
				}
			}
			resultObj.put("children", rootEventType.getChildren());
		}

    	return resultObj;
    }
}
