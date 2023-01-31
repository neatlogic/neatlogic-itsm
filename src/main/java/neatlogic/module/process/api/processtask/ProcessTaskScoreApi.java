package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNoPermissionException;
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
		return "工单评分接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "scoreTemplateId", type = ApiParamType.LONG, isRequired = true, desc = "评分模版ID"),
		@Param(name = "scoreDimensionList", type = ApiParamType.JSONARRAY, isRequired = true,
				desc = "评分维度及分数，格式[{\"id\":133018403841111,\"name\":\"dim\",\"description\":\"see\",\"score\":3}]"),
		@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "评价内容")
	})
	@Output({})
	@Description(desc = "工单评分接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		processTaskVo.setParamObj(jsonObj);
		ProcessStepHandlerFactory.getHandler().scoreProcessTask(processTaskVo);
		return null;
	}

}
