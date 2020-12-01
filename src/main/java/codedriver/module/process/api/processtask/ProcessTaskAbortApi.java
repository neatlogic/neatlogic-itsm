package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
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
		try {
		    ProcessStepHandlerFactory.getHandler().abortProcessTask(processTaskVo);
		}catch(ProcessTaskNoPermissionException e) {
		    throw new PermissionDeniedException();
		}
		return null;
	}

}
