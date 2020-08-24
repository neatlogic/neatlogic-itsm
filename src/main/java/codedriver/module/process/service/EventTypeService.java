package codedriver.module.process.service;

import codedriver.framework.process.dto.event.EventTypeVo;

public interface EventTypeService {
	
	public void rebuildLeftRightCode();

	public EventTypeVo buildRootEventType();

}
