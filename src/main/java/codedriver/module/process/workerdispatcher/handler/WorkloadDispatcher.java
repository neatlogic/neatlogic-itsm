package codedriver.module.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;

@Component
public class WorkloadDispatcher extends WorkerDispatcherBase {
	
	@Autowired
	private TeamMapper teamMapper;
	
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
					/** 找出组员的工单量，按工作量从小到大排序 **/
					List<Map<String, Object>> workloadList = processTaskMapper.getWorkloadByTeamUuid(split[1]);
					if(CollectionUtils.isNotEmpty(workloadList)) {
						int minWorkload = Integer.MAX_VALUE;
						List<String> minWorkloadUserUuidList = new ArrayList<>();
						for(Map<String, Object> workloadMap : workloadList) {
							int count = (int) workloadMap.get("count");
							if(count <= minWorkload) {
								minWorkload = count;
								Object userUuid = workloadMap.get("userUuid");
								if(userUuid != null) {
									minWorkloadUserUuidList.add(userUuid.toString());									
								}
							}else {
								break;
							}
						}
						/** 随机选取一位用户作为处理人 **/
						if(CollectionUtils.isNotEmpty(minWorkloadUserUuidList)) {
							Random random = new Random();
							int index = random.nextInt(minWorkloadUserUuidList.size());
							resultList.add(minWorkloadUserUuidList.get(index));
						}
					}
				}
			}
		}
		return resultList;
	}

	@Override
	public String getHelp() {
		return "在选择的组及父级组中，找出工作量最少（只看待处理和处理中的任务数量）的用户作为当前步骤的处理人";
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
