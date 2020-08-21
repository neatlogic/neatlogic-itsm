package codedriver.module.process.api.processtask;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskStartProcessApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/startprocess";
	}

	@Override
	public String getName() {
		return "工单上报提交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "nextStepId", type = ApiParamType.LONG, isRequired = true, desc = "激活下一步骤Id"),
		@Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "分配步骤处理人信息列表，格式[{\"processTaskStepId\":1, \"processStepUuid\":\"abc\", \"workerList\":[\"user#xxx\",\"team#xxx\",\"role#xxx\"]}]")
	})
	@Description(desc = "工单上报提交接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long nextStepId = jsonObj.getLong("nextStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, null, nextStepId);
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		ProcessTaskStepVo startStepVo = processTaskStepList.get(0);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startStepVo.getHandler());
		if(handler == null) {
            throw new ProcessStepHandlerNotFoundException(startStepVo.getHandler());
		}
        startStepVo.setParamObj(jsonObj);
        handler.startProcess(startStepVo);
		return null;
	}

}
