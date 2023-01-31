package neatlogic.module.process.api.process;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.process.dto.WorkerPolicyVo;
import neatlogic.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkerPolicyListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "process/worker/policy/list";
	}

	@Override
	public String getName() {
		return "指派策略列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Output({@Param(name="Return", explode = WorkerPolicyVo[].class, desc = "指派策略列表")})
	@Description(desc = "指派策略列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return WorkerPolicyHandlerFactory.getAllActiveWorkerPolicy();
	}

}
