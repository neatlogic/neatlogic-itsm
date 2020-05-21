package codedriver.module.process.api.processtask;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskCurrentUserTaskListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
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
		@Param(name = "Return[n].processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "Return[n].stepName", type = ApiParamType.STRING, desc = "步骤名")
	})
	@Description(desc = "当前用户任务列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userUuid = UserContext.get().getUserUuid(true);
		List<String> roleUuidList = UserContext.get().getRoleUuidList();
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
		
		List<Long> processTaskStepIdList = processTaskMapper.getProcessTaskStepIdList(userUuid, teamUuidList, roleUuidList);
		if(CollectionUtils.isEmpty(processTaskStepIdList)) {
			return null;
		}

		String keyword = jsonObj.getString("keyword");
		List<Map<String, Object>> taskList = processTaskMapper.getProcessTaskActiveStepListByStepIdList(keyword, processTaskStepIdList);
		return taskList;
	}

}
