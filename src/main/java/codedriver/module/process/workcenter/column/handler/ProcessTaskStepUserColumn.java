package codedriver.module.process.workcenter.column.handler;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;

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

	@Override
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

	@Override
	public Integer getSort() {
		return 5;
	}
	
	@Override
	public Boolean getMyIsShow() {
		return false;
	}
}
