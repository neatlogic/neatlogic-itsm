package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepUserColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

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
		return "currentstepuser";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤处理人";
	}
	
	@Override
	public Object getMyValue(JSONObject json) {
		JSONArray currentStepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.CURRENT_STEP.getValue());
		if(CollectionUtils.isEmpty(currentStepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		for(Object currentStepObj: currentStepArray) {
			JSONObject currentStepJson = (JSONObject)currentStepObj;
			currentStepJson.put("statusName", ProcessTaskStatus.getText(currentStepJson.getString("status")));
			JSONArray handlerList = currentStepJson.getJSONArray("handlerlist");
			if(CollectionUtils.isEmpty(handlerList)) {
				return CollectionUtils.EMPTY_COLLECTION;
			}
			for(Object handlerObj : handlerList) {
				JSONObject handlerJson = (JSONObject)handlerObj;
				String handler = handlerJson.getString("handler");
				if(handler.startsWith(GroupSearch.USER.getValuePlugin())) {
					UserVo userVo = userMapper.getUserByUserId(handler.replace(GroupSearch.USER.getValuePlugin(), ""));
					if(userVo != null) {
						handlerJson.put("handler", handler);
						handlerJson.put("handlerName", userVo.getUserName());
					}
				}else if(handler.startsWith(GroupSearch.ROLE.getValuePlugin())){
					RoleVo roleVo = roleMapper.getRoleByRoleName(handler.toString().replace(GroupSearch.ROLE.getValuePlugin(), ""));
					if(roleVo != null) {
						handlerJson.put("handler", handler);
						handlerJson.put("handlerName", roleVo.getDescription());
					}
					
				}else if(handler.startsWith(GroupSearch.TEAM.getValuePlugin())){
					TeamVo teamVo = teamMapper.getTeamByUuid(handler.toString().replace(GroupSearch.TEAM.getValuePlugin(), ""));
					if(teamVo != null) {
						handlerJson.put("handler", handler);
						handlerJson.put("handlerName", teamVo.getName());
					}
				}
				
				
			}
			
		}
		return currentStepArray;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

}
