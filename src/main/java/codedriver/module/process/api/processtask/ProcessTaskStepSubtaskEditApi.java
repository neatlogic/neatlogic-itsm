package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
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
public class ProcessTaskStepSubtaskEditApi extends ApiComponentBase {
	
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
		@Param(name = "workerList", type = ApiParamType.STRING, isRequired = true, desc = "子任务处理人userId,单选,格式[\"user#userId\"]"),
		@Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "描述")
	})
	@Output({})
	@Description(desc = "子任务编辑接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String content = jsonObj.getString("content");
		if(StringUtils.isBlank(content)) {
			throw new ParamIrregularException("参数“" + content + "”长度不能为0");
		}
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsEditable().intValue() == 1) {
			List<String> workerList = JSON.parseArray(jsonObj.getString("workerList"), String.class);
			jsonObj.remove("workerList");
			String[] split = workerList.get(0).split("#");
			if(GroupSearch.USER.getValue().equals(split[0])) {
				UserVo userVo = userMapper.getUserByUserId(split[1]);
				if(userVo != null) {
					List<String> oldWorkerList = new ArrayList<>();
					oldWorkerList.add(GroupSearch.USER.getValuePlugin() + processTaskStepSubtaskVo.getUserId());
					jsonObj.put("oldUserId", processTaskStepSubtaskVo.getUserId());
					jsonObj.put("oldUserName", processTaskStepSubtaskVo.getUserName());
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
			processTaskService.editSubtask(processTaskStepSubtaskVo);
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.EDITSUBTASK.getText());
		}
		return null;
	}

}
