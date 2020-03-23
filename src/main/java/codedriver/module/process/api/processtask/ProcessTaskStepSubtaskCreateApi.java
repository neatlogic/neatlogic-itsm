package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepSubtaskCreateApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/step/subtask/create";
	}

	@Override
	public String getName() {
		return "子任务创建接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "workerList", type = ApiParamType.STRING, isRequired = true, desc = "协助处理人userId,单选,格式[\"user#userId\"]"),
		@Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "描述"),
	})
	@Description(desc = "子任务创建接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}

}
