package codedriver.module.process.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class NotifyTriggerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/trigger/list";
	}

	@Override
	public String getName() {
		return "通知触发点列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "通知触发点列表接口")
	@Output({
			@Param(explode = NotifyTemplateVo[].class,
					desc = "通知触发点列表") })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> returnList = new ArrayList<>();
		for (NotifyTriggerType notifyTriggerType : NotifyTriggerType.values()) {
			returnList.add(new ValueTextVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText()));
		}
		return returnList;
	}

}
