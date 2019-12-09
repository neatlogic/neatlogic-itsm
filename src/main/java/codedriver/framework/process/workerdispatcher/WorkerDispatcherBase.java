package codedriver.framework.process.workerdispatcher;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.ProcessTaskStepVo;

public abstract class WorkerDispatcherBase implements IWorkerDispatcher {
	public final List<String> getWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		return myGetWorker(processTaskStepVo, configObj);
	}

	protected abstract List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj);
}
