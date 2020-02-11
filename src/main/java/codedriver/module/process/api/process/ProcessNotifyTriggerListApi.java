package codedriver.module.process.api.process;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessNotifyTriggerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notifytrigger/list";
	}

	@Override
	public String getName() {
		return "通知触发点列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(name="Return", explode=ValueTextVo[].class, desc = "通知触发点列表")
	})
	@Description(desc = "通知触发点列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> processInformPointcutList = new ArrayList<>();;
		for(NotifyTriggerType processTaskStepAction : NotifyTriggerType.values()) {
			processInformPointcutList.add(new ValueTextVo(processTaskStepAction.getTrigger(), processTaskStepAction.getText()));
		}
		return processInformPointcutList;
	}

}
