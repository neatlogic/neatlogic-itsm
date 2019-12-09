package codedriver.framework.process.workerdispatcher.core;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.dto.ProcessTaskStepVo;

public abstract class WorkerDispatcherBase implements IWorkerDispatcher {
	public final List<String> getWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		return myGetWorker(processTaskStepVo, configObj);
	}

	protected abstract List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj);
}
