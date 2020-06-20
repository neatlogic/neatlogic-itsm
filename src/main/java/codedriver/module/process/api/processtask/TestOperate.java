package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.operate.core.OperateHandlerType;
import codedriver.framework.process.operate.core.ProcessOperateManager;
import codedriver.framework.restful.core.ApiComponentBase;

@Component
public class TestOperate extends ApiComponentBase {

	@Override
	public String getToken() {
		return "operate/test";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "测试";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// ProcessOperate operate = new
		ProcessOperateManager ProcessOperateBuilder = new ProcessOperateManager.Builder().setNext(OperateHandlerType.START).setNext(OperateHandlerType.SUBTASK).setNext(OperateHandlerType.AUTOMATIC).build();
		return ProcessOperateBuilder.getOperateList(jsonObj.getLong("processTaskId"), jsonObj.getLong("processTaskStepId"));
	}

}
