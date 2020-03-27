package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskActionColumn implements IWorkcenterColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterField.ACTION.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterField.ACTION.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		/*JSONObject commonJson = (JSONObject) el.getJSON(ProcessFieldType.COMMON.getValue());
		if(commonJson == null) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray currentStepArray = (JSONArray) commonJson.getJSONArray(ProcessWorkcenterField.CURRENT_STEP.getValue());
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
			
		}*/
		return "";
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 100;
	}

}
