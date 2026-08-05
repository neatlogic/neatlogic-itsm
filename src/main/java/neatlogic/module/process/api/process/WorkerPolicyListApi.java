package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.WorkerPolicyVo;
import neatlogic.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

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
		return "nmpap.workerpolicylistapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Output({@Param(name="Return", explode = WorkerPolicyVo[].class, desc = "nmpap.workerpolicylistapi.output.param.desc.return.name")})
	@Description(desc = "nmpap.workerpolicylistapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return WorkerPolicyHandlerFactory.getAllActiveWorkerPolicy();
	}

}
