package codedriver.module.process.api.workerdispatcher;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActive;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.dto.WorkerDispatcherVo;

@IsActive
@Service
public class WorkerDispatcherHandlerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "worker/dispather/handler/list";
	}

	@Override
	public String getName() {
		return "分派器列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({@Param(explode = WorkerDispatcherVo.class)})
	@Description(desc="分派器列表接口")
	
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return WorkerDispatcherFactory.getAllActiveWorkerDispatcher();
	}

}
