package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepSubtaskNotFoundException;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskStepSubtaskEditApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "processtask/step/subtask/edit";
	}

	@Override
	public String getName() {
		return "子任务编辑接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskStepSubtaskId", type = ApiParamType.LONG, isRequired = true, desc = "子任务id"),
		@Param(name = "workerList", type = ApiParamType.STRING, isRequired = true, desc = "子任务处理人userUuid,单选,格式user#userUuid"),
		@Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "描述")
	})
	@Output({})
	@Description(desc = "子任务编辑接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsEditable().intValue() == 1) {
			String workerList = jsonObj.getString("workerList");
			if(workerList.startsWith(GroupSearch.USER.getValuePlugin())) {
				String[] split = workerList.split("#");
				UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
				if(userVo == null) {
					throw new UserNotFoundException(split[1]);
				}
			}else {
				throw new ProcessTaskRuntimeException("子任务处理人不能为空");
			}
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			processTaskService.editSubtask(processTaskStepSubtaskVo);
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.EDITSUBTASK.getText());
		}
		return null;
	}

}
