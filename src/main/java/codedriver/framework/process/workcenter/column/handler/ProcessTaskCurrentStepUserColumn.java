package codedriver.framework.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Component
public class ProcessTaskCurrentStepUserColumn implements IWorkcenterColumn{

	@Autowired
	ProcessTaskMapper processTaskMapper;
	@Autowired
	UserMapper userMapper;
	@Autowired
	RoleMapper roleMapper;
	@Autowired
	TeamMapper teamMapper;
	
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_USER.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_USER.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		JSONArray currentStepIdArray = (JSONArray) JSONArray.parse(el.getString(ProcessWorkcenterCondition.CURRENT_STEP.getValue()));
		JSONObject currentStepWorkerJson = (JSONObject) JSONObject.parse(el.getString(this.getName()));
		JSONArray resultArray = new JSONArray(); 
		
		for(Object currentStepId: currentStepIdArray) {
			JSONObject resultObject = new JSONObject();
			Long stepId =Long.valueOf(currentStepId.toString());
			String cacheKey = "processTaskStep#"+stepId;
			String stepName = (String) WorkcenterColumnDataCache.getItem(cacheKey);
			if(stepName == null) {
				ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepId);
				if(stepVo != null) {
					stepName = stepVo.getName();
					WorkcenterColumnDataCache.addItem(cacheKey, stepName);
				}
			}
			resultObject.put("stepId",stepId);
			resultObject.put("stepName",stepName);
			JSONArray resultWorkerArray = new JSONArray();
			JSONArray currentStepWorkArray = currentStepWorkerJson.getJSONArray(stepId.toString());
			for(Object workerObj: currentStepWorkArray) {
				JSONObject resultWorkerJson = new JSONObject();
				String worker = workerObj.toString();
				String userType = null;
				if(worker.contains("#@")) {
					String[] tmp = worker.split("#@");
					worker = tmp[0];
					userType = tmp[1];
				}
				String valueName = (String) WorkcenterColumnDataCache.getItem(worker.toString());
				if(valueName == null) {
					if(worker.toString().startsWith(GroupSearch.USER.getValue())) {
						UserVo userVo= userMapper.getUserByUserId(worker.toString().replace(GroupSearch.USER.getValue()+"#", ""));
						if(userVo != null) {
							valueName = userVo.getUserName();
							WorkcenterColumnDataCache.addItem(worker.toString(), valueName);
							
						}
					}
					if(worker.toString().startsWith(GroupSearch.ROLE.getValue())) {
						RoleVo roleVo = roleMapper.getRoleByRoleName(worker.toString().replace(GroupSearch.ROLE.getValue()+"#", ""));
						if(roleVo != null) {
							valueName = roleVo.getDescription();
							WorkcenterColumnDataCache.addItem(worker.toString(), valueName);
						}
					}
					if(worker.toString().startsWith(GroupSearch.TEAM.getValue())) {
						TeamVo teamVo = teamMapper.getTeamByUuid(worker.toString().replace(GroupSearch.TEAM.getValue()+"#", ""));
						if(teamVo != null) {
							valueName = teamVo.getName();
							WorkcenterColumnDataCache.addItem(worker.toString(), valueName);
						}
					}
				}
				resultWorkerJson.put("value", worker);
				resultWorkerJson.put("text", valueName);
				resultWorkerJson.put("userType", UserType.getText(userType));
				resultWorkerArray.add(resultWorkerJson);
			}
			resultObject.put("currentStepWorkerList", resultWorkerArray);
			resultArray.add(resultObject);
		}
		return resultArray;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
