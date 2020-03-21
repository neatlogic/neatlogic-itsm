package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;

@Service
public class ProcessTaskAbortApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		if(!processTaskService.verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.ABORT)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		processTaskVo.setConfig(processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash()).getConfig());
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler();
		handler.abortProcessTask(processTaskVo);
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.END.getValue());
		//生成活动
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo(processTaskId, processTaskStepList.get(0).getId(), UserContext.get().getUserId(true), ProcessTaskStepAction.ABORT.getValue());
		processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
		return null;
	}

}
