package codedriver.framework.process.workerdispatcher.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Component
public class WorkloadDispatcher extends WorkerDispatcherBase {

	@Override
	public String getHandler() {
		return this.getClass().getName();
	}

	@Override
	public String getName() {
		return "根据工作量分配处理人";
	}

	

	@Override
	protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHelp() {
		return "在处理人范围中";
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

}
