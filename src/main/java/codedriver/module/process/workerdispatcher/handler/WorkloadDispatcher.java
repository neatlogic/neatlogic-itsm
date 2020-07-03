package codedriver.module.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;

@Component
public class WorkloadDispatcher extends WorkerDispatcherBase {
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "根据工作量分配处理人";
	}	

	@Override
	protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		List<String> resultList = new ArrayList<>();
		if(MapUtils.isNotEmpty(configObj)) {
			String team = configObj.getString("team");
			if(StringUtils.isNotBlank(team) && team.startsWith(GroupSearch.TEAM.getValuePlugin())) {
				String[] split = team.split("#");
				List<TeamUserVo> teamUserList = teamMapper.getTeamUserListByTeamUuid(split[1]);
				if(CollectionUtils.isNotEmpty(teamUserList)) {
					String targerUserUuid = null;
					int minWorkload = Integer.MAX_VALUE;
					for(TeamUserVo teamUser : teamUserList) {
						List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(teamUser.getUserUuid());
						List<String> teamUuidList = userMapper.getTeamUuidListByUserUuid(teamUser.getUserUuid());
						/** 查出每个组员的工单量 **/
						List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerListByUserUuidTeamUuidListRoleUuidList(teamUser.getUserUuid(), teamUuidList, roleUuidList);
						/** 找出工单量较小的组员 **/
						if(processTaskStepWorkerList.size() < minWorkload) {
							minWorkload = processTaskStepWorkerList.size();
							targerUserUuid = teamUser.getUserUuid();
						}						
					}
					if(StringUtils.isNotBlank(targerUserUuid)) {
						resultList.add(targerUserUuid);
					}
				}
			}
		}
		return resultList;
	}

	@Override
	public String getHelp() {
		return "在处理人范围中";
	}

	@Override
	public JSONArray getConfig() {
		JSONArray resultArray = new JSONArray();
		/** 选择处理组 **/
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", "userselect");
		jsonObj.put("name", "team");
		jsonObj.put("label", "处理组");
		jsonObj.put("validateList", Arrays.asList("required"));
		jsonObj.put("multiple", false);
		jsonObj.put("value", "");
		jsonObj.put("defaultValue", "");
		jsonObj.put("groupList", Arrays.asList("team"));
		resultArray.add(jsonObj);
		return resultArray;
	}

}
