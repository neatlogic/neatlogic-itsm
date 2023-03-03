package neatlogic.module.process.api.process;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.process.dto.WorkerDispatcherVo;
import neatlogic.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkerDispatcherListApi extends PrivateApiComponentBase {
	
	@Override
	public String getToken() {
		return "process/workerdispatcher/list";
	}

	@Override
	public String getName() {
		return "处理人分派器列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Output({
			@Param(name = "workerDispatcherList",
					explode = WorkerDispatcherVo[].class,
					desc = "处理人分派器列表") })
	@Description(desc = "处理人分派器列表接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return WorkerDispatcherFactory.getAllActiveWorkerDispatcher();
	}
	
}
