package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepSubtaskNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
public class ProcessTaskStepSubtaskCommentApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/step/subtask/comment";
	}

	@Override
	public String getName() {
		return "子任务回复接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskStepSubtaskId", type = ApiParamType.LONG, isRequired = true, desc = "子任务id"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "描述")
	})
	@Output({})
	@Description(desc = "子任务回复接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsCommentable().intValue() == 1) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			processTaskService.commentSubtask(processTaskStepSubtaskVo);
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.COMMENTSUBTASK.getText());
		}
		return null;
	}

}
