package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskStepUserColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Autowired
	RoleMapper roleMapper;
	@Autowired
	TeamMapper teamMapper;

	@Override
	public String getName() {
		return "stepuser";
	}

	@Override
	public String getDisplayName() {
		return "步骤处理人";
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		return null;
	}
	
	@Override
	public Object getMyValueText(JSONObject json) {
		JSONArray userArrayResult = new JSONArray();
		Map<String,JSONObject> userMap = new HashMap<String,JSONObject>();
		JSONArray stepArray = null;
		try {
		 stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		}catch(Exception ex){
			return null;
		}
		if(CollectionUtils.isEmpty(stepArray)) {
			return null;
		}
		JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
		ListIterator<Object> stepIterator = stepResultArray.listIterator();
		while(stepIterator.hasNext()) {
			JSONObject currentStepJson = (JSONObject)stepIterator.next();
			JSONArray userTypeArray = currentStepJson.getJSONArray("usertypelist"); 
			if(CollectionUtils.isNotEmpty(userTypeArray)) {
				ListIterator<Object> userTypeIterator = userTypeArray.listIterator();
				while(userTypeIterator.hasNext()) {
					JSONObject userTypeJson = (JSONObject) userTypeIterator.next();
					if(ProcessTaskStatus.PENDING.getValue().equals(userTypeJson.getString("usertype"))) {
						JSONArray userArray = userTypeJson.getJSONArray("userlist");
						if(CollectionUtils.isNotEmpty(userArray)) {
							for(Object userObject :userArray) {
								String user = userObject.toString();
								if(StringUtils.isNotBlank(user)) {
									if(user.startsWith(GroupSearch.USER.getValuePlugin())) {
										String userUuid = user.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY);
										UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid);
										if(userVo != null) {
											JSONObject userJson = new JSONObject();
											userJson.put("value", userUuid);
											userJson.put("text", userVo.getUserName());
											userMap.put(userUuid, userJson);
										}
									}else if(user.startsWith(GroupSearch.ROLE.getValuePlugin())) {
										UserVo userVo = new UserVo();
										userVo.setRoleUuid(user.replaceFirst(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
										userVo.setNeedPage(false);
										for(UserVo roleUser: userMapper.searchUser(userVo)) {
											JSONObject userJson = new JSONObject();
											userJson.put("value", roleUser.getUuid());
											userJson.put("text", roleUser.getUserName());
											userMap.put(roleUser.getUuid(), userJson);
										}
									}else if(user.startsWith(GroupSearch.TEAM.getValuePlugin())) {
										UserVo userVo = new UserVo();
										userVo.setTeamUuid(user.replaceFirst(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
										userVo.setNeedPage(false);
										for(UserVo teamUser: userMapper.searchUser(userVo)) {
											JSONObject userJson = new JSONObject();
											userJson.put("value", teamUser.getUuid());
											userJson.put("text", teamUser.getUserName());
											userMap.put(teamUser.getUuid(), userJson);
										}
									}
								}
								
							}
						}
					}else {
						JSONArray userArray = userTypeJson.getJSONArray("userlist");
						if(CollectionUtils.isNotEmpty(userArray)) {
							List<String> userList = userArray.stream().map(object -> object.toString()).collect(Collectors.toList());
							for(String user :userList) {
								if(StringUtils.isNotBlank(user)) {
									String userUuid = user.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY);
									UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid);
									if(userVo != null) {
										JSONObject userJson = new JSONObject();
										userJson.put("value", userUuid);
										userJson.put("text", userVo.getUserName());
										userMap.put(userUuid, userJson);
									}
								}
								
							}
						}
					}
				}
			}
		}
		for(Entry<String, JSONObject> entry :userMap.entrySet()) {
			userArrayResult.add(entry.getValue());
		}
		return userArrayResult;
	}*/

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

	@Override
	public Integer getSort() {
		return 5;
	}
	
	@Override
	public Boolean getMyIsShow() {
		return false;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		return null;
	}*/

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return null;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return SqlTableUtil.getTableSelectColumn();
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return SqlTableUtil.getStepUserJoinTableSql();
	}

	@Override
	public void getMyDashboardDataVo(DashboardDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
		if (getName().equals(workcenterVo.getDashboardConfigVo().getGroup())) {
			DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo("stepUserUserUuid", workcenterVo.getDashboardConfigVo().getGroup(), "stepUserUserName", workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
			dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
		}
		//如果存在子分组
		if (getName().equals(workcenterVo.getDashboardConfigVo().getSubGroup())) {
			DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
			dashboardDataSubGroupVo = new DashboardDataSubGroupVo("stepUserUserUuid", workcenterVo.getDashboardConfigVo().getSubGroup(), "stepUserUserName");
			dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
		}
	}

	@Override
	public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
		LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
		for (Map<String, Object> dataMap : mapList) {
			if(dataMap.containsKey("stepUserUserUuid")) {
				groupDataMap.put(dataMap.get("stepUserUserUuid").toString(), dataMap.get("count"));
			}
		}
		return groupDataMap;
	}
}
