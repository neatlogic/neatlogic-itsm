package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskRecoverApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/recover";
	}

	@Override
	public String getName() {
		return "工单恢复接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id")
	})
	@Output({})
	@Description(desc = "工单恢复接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		processTaskVo.setConfig(processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash()).getConfig());
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler();
		handler.recoverProcessTask(processTaskVo);
		return null;
	}

}
