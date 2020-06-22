package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;
import codedriver.framework.process.operationauth.core.ProcessOperateManager;
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
		ProcessOperateManager ProcessOperateBuilder = new ProcessOperateManager.Builder().setNext(OperationAuthHandlerType.START).setNext(OperationAuthHandlerType.SUBTASK).setNext(OperationAuthHandlerType.AUTOMATIC).build();
		return ProcessOperateBuilder.getOperateList(jsonObj.getLong("processTaskId"), jsonObj.getLong("processTaskStepId"));
	}

}
