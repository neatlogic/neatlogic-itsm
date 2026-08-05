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
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskScoreApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/score";
	}

	@Override
	public String getName() {
		return "nmpap.processtaskscoreapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskscoreapi.input.param.desc.processtaskid"),
		@Param(name = "scoreTemplateId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskscoreapi.input.param.desc.scoretemplateid"),
		@Param(name = "scoreDimensionList", type = ApiParamType.JSONARRAY, isRequired = true,
				desc = "nmpap.processtaskscoreapi.input.param.desc.scoredimensionlist"),
		@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskscoreapi.input.param.desc.source"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "nmpap.processtaskscoreapi.input.param.desc.content")
	})
	@Output({})
	@Description(desc = "nmpap.processtaskscoreapi.getname")
	@ResubmitInterval(3)
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		processTaskVo.setParamObj(jsonObj);
		ProcessStepHandlerFactory.getHandler().scoreProcessTask(processTaskVo);
		return null;
	}

}
