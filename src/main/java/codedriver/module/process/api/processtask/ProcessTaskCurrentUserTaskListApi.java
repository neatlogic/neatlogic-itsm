package codedriver.module.process.api.processtask;

import java.util.HashMap;
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
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
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
	
	@Autowired
	private WorktimeMapper worktimeMapper;
	
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
		if(CollectionUtils.isNotEmpty(processTaskStepIdList)) {
			String keyword = jsonObj.getString("keyword");
			List<Map<String, Object>> taskList = processTaskMapper.getProcessTaskActiveStepListByStepIdList(keyword, processTaskStepIdList);
			if(CollectionUtils.isNotEmpty(taskList)) {
				if(taskList.size() != processTaskStepIdList.size()) {
					processTaskStepIdList.clear();
					for(Map<String, Object> task : taskList) {
						processTaskStepIdList.add(Long.parseLong(task.get("processTaskStepId").toString()));
					}
				}
				Map<Long, ProcessTaskSlaTimeVo> stepSlaTimeMap = new HashMap<>();
				List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskMapper.getProcessTaskSlaTimeByProcessTaskStepIdList(processTaskStepIdList);
				for(ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
					if(!stepSlaTimeMap.containsKey(processTaskSlaTimeVo.getProcessTaskStepId())) {
						stepSlaTimeMap.put(processTaskSlaTimeVo.getProcessTaskStepId(), processTaskSlaTimeVo);
					}
				}
				for(Map<String, Object> task : taskList) {
					ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
					processTaskStepVo.setStatus(task.get("status").toString());
					String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(task.get("configHash").toString());
					processTaskStepVo.setConfig(stepConfig);
					task.put("statusVo", processTaskStepVo.getStatusVo());
					Long stepId = Long.parseLong(task.get("processTaskStepId").toString());
					
					ProcessTaskSlaTimeVo processTaskSlaTimeVo = stepSlaTimeMap.get(stepId);
					if(processTaskSlaTimeVo.getExpireTime() != null) {
						long timeLeft = worktimeMapper.calculateCostTime(task.get("worktimeUuid").toString(), System.currentTimeMillis(), processTaskSlaTimeVo.getExpireTime().getTime());
						processTaskSlaTimeVo.setTimeLeft(timeLeft);
						processTaskSlaTimeVo.setTimeLeftDesc(conversionTimeUnit(timeLeft));
					}
					if(processTaskSlaTimeVo.getRealExpireTime() != null) {
						long realTimeLeft = processTaskSlaTimeVo.getExpireTime().getTime() - System.currentTimeMillis();
						processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
						processTaskSlaTimeVo.setRealTimeLeftDesc(conversionTimeUnit(realTimeLeft));
					}
					task.put("slaTime", processTaskSlaTimeVo);
				}
			}
			
			return taskList;
		}
		return null;
	}
	
	private String conversionTimeUnit(long milliseconds) {
		StringBuilder stringBuilder = new StringBuilder();
		milliseconds = Math.abs(milliseconds);
		if(milliseconds >= (60 * 60 * 1000)) {
			long hours = milliseconds / (60 * 60 * 1000);
			stringBuilder.append(hours);
			stringBuilder.append("小时");
			milliseconds = milliseconds % (60 * 60 * 1000);
		}
		if(milliseconds >= (60 * 1000)) {
			long minutes = milliseconds / (60 * 1000);
			stringBuilder.append(minutes);
			stringBuilder.append("分钟");
			milliseconds = milliseconds % (60 * 1000);
		}
		if(milliseconds >= 1000) {
			long seconds = milliseconds / 1000;
			stringBuilder.append(seconds);
			stringBuilder.append("秒");
		}
		return stringBuilder.toString();
	}
}
