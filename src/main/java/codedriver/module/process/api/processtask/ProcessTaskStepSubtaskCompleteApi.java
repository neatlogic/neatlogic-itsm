package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepSubtaskCompleteApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/step/subtask/complete";
	}

	@Override
	public String getName() {
		return "子任务完成接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
