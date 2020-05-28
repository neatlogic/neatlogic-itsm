package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyTriggerHandler;
import codedriver.framework.process.notify.core.NotifyTriggerType;

public class ProcessNotifyTriggerHandler implements INotifyTriggerHandler{

	@Override
	public List<ValueTextVo> getNotifyTriggerList() {
		List<ValueTextVo> returnList = new ArrayList<>();
		for (NotifyTriggerType notifyTriggerType : NotifyTriggerType.values()) {
			if(NotifyTriggerType.TIMEOUT == notifyTriggerType) {
				continue;
			}
			returnList.add(new ValueTextVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText()));
		}
		return returnList;
	}

}
