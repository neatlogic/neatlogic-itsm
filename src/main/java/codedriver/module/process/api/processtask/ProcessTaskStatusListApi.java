package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStatus;

@Service
public class ProcessTaskStatusListApi extends ApiComponentBase {

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

	@Description(desc = "工单状态列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return ProcessTaskStatus.getProcessTaskStatusList();
	}

}
