package codedriver.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

//@Service
public class ProcessTaskStatusListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/status/list";
	}

	@Override
	public String getName() {
		return "工单状态列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Output({
		@Param(name="Return", explode=ValueTextVo[].class, desc = "工单状态列表")
	})
	@Description(desc = "工单状态列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return ProcessTaskStatus.getProcessTaskStatusList();
	}

}

