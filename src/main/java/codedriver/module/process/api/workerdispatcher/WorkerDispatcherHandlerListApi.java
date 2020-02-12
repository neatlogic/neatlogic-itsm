package codedriver.module.process.api.workerdispatcher;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class WorkerDispatcherHandlerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "worker/dispather/handler/list";
	}

	@Override
	public String getName() {
		return "分派器列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return WorkerDispatcherFactory.getAllActiveWorkerDispatcher();
	}

}
