package codedriver.module.process.api.processtask;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStepSubtaskCreateApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/step/subtask/create";
	}

	@Override
	public String getName() {
		return "子任务创建接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "workerList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "子任务处理人userId,单选,格式[\"user#userId\"]"),
		@Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "描述")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.LONG, desc = "子任务id")
	})
	@Description(desc = "子任务创建接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		//获取步骤信息
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		Long processTaskId = processTaskStepVo.getProcessTaskId();
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.CREATESUBTASK)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.CREATESUBTASK.getText());
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
			processTaskStepSubtaskVo.setProcessTaskId(processTaskId);
			processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
			processTaskStepSubtaskVo.setOwner(UserContext.get().getUserId(true));
			List<String> workerList = JSON.parseArray(jsonObj.getString("workerList"), String.class);
			String[] split = workerList.get(0).split("#");
			if(GroupSearch.USER.getValue().equals(split[0])) {
				UserVo userVo = userMapper.getUserByUserId(split[1]);
				if(userVo != null) {
					processTaskStepSubtaskVo.setUserId(userVo.getUserId());
					processTaskStepSubtaskVo.setUserName(userVo.getUserName());
				}else {
					throw new UserNotFoundException(split[1]);
				}
			}else {
				throw new ProcessTaskRuntimeException("子任务处理人不能为空");
			}
			
			Long targetTime = jsonObj.getLong("targetTime");
			if(targetTime != null) {
				processTaskStepSubtaskVo.setTargetTime(new Date(targetTime));
			}
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			handler.createSubtask(processTaskStepSubtaskVo);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}

}
