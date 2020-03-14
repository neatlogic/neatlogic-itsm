package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskNextStepListApi extends ApiComponentBase{

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processTask/nextstep/list";
	}

	@Override
	public String getName() {
		return "下一可流转步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "下一可流转步骤列表")
	})
	@Description(desc = "下一可流转步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.COMPLETE)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		return processStepHandler.getNext(currentProcessTaskStepVo);
	}

}
