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
