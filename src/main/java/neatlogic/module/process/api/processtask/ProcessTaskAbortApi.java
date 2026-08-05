package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
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
		return "nmpap.processtaskabortapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "nmpap.processtaskabortapi.input.param.desc.processtaskid", isRequired = true),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtaskabortapi.input.param.desc.content"),
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskabortapi.input.param.desc.source")
	})
	@Output({})
	@Description(desc = "nmpap.processtaskabortapi.getname")
	@ResubmitInterval(3)
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		processTaskVo.getParamObj().put("source", jsonObj.getString("source"));
		processTaskVo.getParamObj().put("content", jsonObj.getString("content"));
		ProcessStepHandlerFactory.getHandler().abortProcessTask(processTaskVo);
		return null;
	}

}
