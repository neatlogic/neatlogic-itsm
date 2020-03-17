package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.PriorityVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class ProcessTaskCurrentUserTaskListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "processtask/currentuser/task/list";
	}

	@Override
	public String getName() {
		return "当前用户任务列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字搜索"),
	})
	@Output({
		@Param(name = "Return[n].processTaskId", type = ApiParamType.LONG, desc = "工单id"),
		@Param(name = "Return[n].title", type = ApiParamType.STRING, desc = "工单标题"),
		@Param(name = "Return[n].status", type = ApiParamType.STRING, desc = "工单状态"),
		@Param(name = "Return[n].statusText", type = ApiParamType.STRING, desc = "工单状态名"),
		@Param(name = "Return[n].priority", explode = PriorityVo.class, desc = "工单优先级"),
		@Param(name = "Return[n].processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "Return[n].stepName", type = ApiParamType.STRING, desc = "步骤名"),
		@Param(name = "Return[n].stepStatus", type = ApiParamType.STRING, desc = "步骤状态"),
		@Param(name = "Return[n].stepStatusText", type = ApiParamType.STRING, desc = "步骤状态名")
	})
	@Description(desc = "当前用户任务列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = UserContext.get().getUserId(true);
		List<String> roleNameList = UserContext.get().getRoleNameList();
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserId(UserContext.get().getUserId(true));
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerList(userId, teamUuidList, roleNameList);
		if(CollectionUtils.isEmpty(processTaskStepWorkerList)) {
			return null;
		}
		String keyword = jsonObj.getString("keyword");
		JSONArray taskList = new JSONArray();
		for(ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskStepWorkerVo.getProcessTaskId());
			if(StringUtils.isNotBlank(keyword) && !processTaskVo.getTitle().contains(keyword)) {
				continue;
			}
			PriorityVo priority = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepWorkerVo.getProcessTaskStepId());
			JSONObject task = new JSONObject();
			task.put("processTaskId", processTaskVo.getId());
			task.put("title", processTaskVo.getTitle());
			task.put("status", processTaskVo.getStatus());
			task.put("statusText", processTaskVo.getStatusText());
			task.put("priority", priority);
			task.put("processTaskStepId", processTaskStepVo.getId());
			task.put("stepName", processTaskStepVo.getName());
			task.put("stepStatus", processTaskStepVo.getStatus());
			task.put("stepStatusText", processTaskStepVo.getStatusText());
			taskList.add(task);
		}
		return taskList;
	}

}
