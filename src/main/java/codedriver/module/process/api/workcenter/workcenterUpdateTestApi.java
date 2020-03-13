package codedriver.module.process.api.workcenter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.WorkcenterEsHandler;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Transactional
@Service
public class workcenterUpdateTestApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "workcenter/test/update";
	}

	@Override
	public String getName() {
		return "测试工单中心update接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试工单中心update接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessTaskStepVo currentProcessTaskStepVo = new ProcessTaskStepVo();
		currentProcessTaskStepVo.setProcessTaskId(jsonObj.getLong("processTaskId"));
		WorkcenterEsHandler.update(currentProcessTaskStepVo);
		return "OK";
	}

}
