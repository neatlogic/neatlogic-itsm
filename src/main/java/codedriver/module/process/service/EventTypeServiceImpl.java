package codedriver.module.process.service;

import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTypeServiceImpl implements EventTypeService {

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public void rebuildLeftRightCode() {
		rebuildLeftRightCode(EventTypeVo.ROOT_ID, 1);
	}
	
	private Integer rebuildLeftRightCode(Long parentId, Integer parentLft) {
		List<EventTypeVo> eventTypeVoList = eventTypeMapper.getEventTypeByParentId(parentId);
		for(EventTypeVo eventType : eventTypeVoList) {
			if(eventType.getChildCount() == 0) {
				eventTypeMapper.updateEventTypeLeftRightCode(eventType.getId(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			}else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(eventType.getId(), lft);
				eventTypeMapper.updateEventTypeLeftRightCode(eventType.getId(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}

	@Override
	public EventTypeVo buildRootEventType() {
		Integer maxRhtCode = eventTypeMapper.getMaxRhtCode();
		EventTypeVo rootEventType = new EventTypeVo();
		rootEventType.setId(EventTypeVo.ROOT_ID);
		rootEventType.setName("root");
		rootEventType.setParentId(EventTypeVo.ROOT_PARENTID);
		rootEventType.setLft(1);
		rootEventType.setRht(maxRhtCode == null ? 2 : maxRhtCode + 1);
		return rootEventType;
	}
}
