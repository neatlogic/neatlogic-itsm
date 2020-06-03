package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerBase;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.process.notify.core.NotifyTriggerType;
@Component
public class TestNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return "TEST";
	}
	
	@Override
	public List<ValueTextVo> myNotifyTriggerList() {
		List<ValueTextVo> returnList = new ArrayList<>();
		for (NotifyTriggerType notifyTriggerType : NotifyTriggerType.values()) {
			if(NotifyTriggerType.TIMEOUT == notifyTriggerType) {
				continue;
			}
			returnList.add(new ValueTextVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText()));
		}
		return returnList;
	}

	@Override
	protected List<NotifyPolicyParamVo> mySystemParamList() {
		List<NotifyPolicyParamVo> notifyPolicyParamList = new ArrayList<>();
		return notifyPolicyParamList;
	}

}
