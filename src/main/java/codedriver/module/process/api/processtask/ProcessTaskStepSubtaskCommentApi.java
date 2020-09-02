package codedriver.module.process.api.processtask;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepSubtaskNotFoundException;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskStepSubtaskCommentApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

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
	@Output({
		@Param(name = "contentList", explode = ProcessTaskStepSubtaskContentVo[].class, desc = "评论列表")
	})
	@Description(desc = "子任务回复接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsCommentable().intValue() == 1) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			List<ProcessTaskStepSubtaskContentVo> contentList = processTaskStepSubtaskService.commentSubtask(processTaskStepSubtaskVo);
			resultObj.put("contentList", contentList);
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskOperationType.COMMENTSUBTASK.getText());
		}
		return resultObj;
	}

}
