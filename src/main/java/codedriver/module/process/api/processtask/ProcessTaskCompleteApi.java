package codedriver.module.process.api.processtask;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
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
public class ProcessTaskCompleteApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/complete";
	}

	@Override
	public String getName() {
		return "工单完成接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
		@Param(name = "nextStepId", type = ApiParamType.LONG, isRequired = true, desc = "激活下一步骤Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", isRequired = true, desc = "操作类型"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "原因"),
		@Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "分配步骤处理人信息列表，格式[{\"processTaskStepId\":1, \"workerList\":[\"user#xxx\",\"team#xxx\",\"role#xxx\"]}]")
	})
	@Description(desc = "工单完成接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        Long nextStepId = jsonObj.getLong("nextStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, nextStepId);
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler == null) {
		    throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
        processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus())) {
                //如果还有子任务未完成，该步骤不能流转
                throw new ProcessTaskRuntimeException("请完成所有子任务后再流转");
            }
        }
        processTaskStepVo.setParamObj(jsonObj);
        handler.complete(processTaskStepVo);
		return null;
	}

}
