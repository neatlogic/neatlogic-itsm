package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAbortApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/abort";
	}

	@Override
	public String getName() {
		return "工单取消接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Output({})
	@Description(desc = "工单取消接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		ProcessStepHandlerFactory.getHandler().abortProcessTask(processTaskVo);
		return null;
	}

}
