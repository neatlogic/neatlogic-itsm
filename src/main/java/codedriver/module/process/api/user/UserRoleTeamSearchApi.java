package codedriver.module.process.api.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.UserType;
@Service
public class UserRoleTeamSearchApi extends ApiComponentBase {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "user/role/team/search";
	}

	@Override
	public String getName() {
		return "用户角色及组织架构查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", isRequired = false, xss = true),
		@Param(name = "valueList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "用于回显的参数列表"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10", isRequired = false),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页", isRequired = false)
		})
	@Output({
		@Param(name="text", type = ApiParamType.STRING, desc="类型中文名"),
		@Param(name="value", type = ApiParamType.STRING, desc="类型"),
		@Param(name="dataList[0].text", type = ApiParamType.STRING, desc="类型具体选项名"),
		@Param(name="dataList[0].value", type = ApiParamType.STRING, desc="类型具体选项值")
	})
	@Description(desc = "用户角色及组织架构查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		final String USER_HEADER = "user#";
		final String ROLE_HEADER = "role#";
		final String TEAM_HEADER = "team#";
		final String COMMON_HEADER = "common#";
		List<UserVo> userList = new ArrayList<UserVo>();
		List<RoleVo> roleList = new ArrayList<RoleVo>();
		List<TeamVo> teamList = new ArrayList<TeamVo>();
		List<String> commonList = new ArrayList<String>();
		if(jsonObj.containsKey("keyword")) {
			UserVo userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>() {});
			if (userVo.getNeedPage()) {
				int rowNum = userMapper.searchUserCount(userVo);
				userVo.setRowNum(rowNum);
				userVo.setPageCount(PageUtil.getPageCount(rowNum, userVo.getPageSize()));
			}
			userList = userMapper.searchUser(userVo);
			
			RoleVo roleVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<RoleVo>() {});
			if(roleVo.getNeedPage()) {
				int rowNum = roleMapper.searchRoleCount(roleVo);
				roleVo.setPageCount(PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
				roleVo.setRowNum(rowNum);
			}
			roleList = roleMapper.searchRole(roleVo);
			
			TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});		
			if (teamVo.getNeedPage()) {
				int rowNum = teamMapper.searchTeamCount(teamVo);
				teamVo.setRowNum(rowNum);
				teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
			}
			teamList = teamMapper.searchTeam(teamVo);
		}else {//回显
			if(jsonObj.containsKey("valueList") && !jsonObj.getJSONArray("valueList").isEmpty()) {
				List<String> userIdList = new ArrayList<String>();
				List<String> roleNameList = new ArrayList<String>();
				List<String> teamUuidList = new ArrayList<String>();
				for(Object value :jsonObj.getJSONArray("valueList")) {
					if(value.toString().startsWith(USER_HEADER)) {
						userIdList.add(value.toString().replace(USER_HEADER, ""));
					}else if(value.toString().startsWith(TEAM_HEADER)){
						teamUuidList.add(value.toString().replace(TEAM_HEADER, ""));
					}else if(value.toString().startsWith(ROLE_HEADER)){
						roleNameList.add(value.toString().replace(ROLE_HEADER, ""));
					}else if(value.toString().startsWith(COMMON_HEADER)){
						commonList.add(value.toString().replace(COMMON_HEADER, ""));
					}
				}
				if(userIdList.size()>0) {
					userList = userMapper.getUserByUserIdList(userIdList);
				}
				if(roleNameList.size()>0) {
					roleList = roleMapper.getRoleByRoleNameList(roleNameList);
				}
				if(teamUuidList.size()>0) {
					teamList = teamMapper.getTeamByUuidList(teamUuidList);
				}
			}
		}
		
		
		JSONArray resultArray = new JSONArray();
		//用户
		JSONObject userObj = new JSONObject();
		userObj.put("value", "user");
		userObj.put("text", "用户");
		JSONArray userArray = new JSONArray();
		for(UserVo user:userList) {
			JSONObject userTmp = new JSONObject();
			userTmp.put("value", USER_HEADER+user.getUserId());
			userTmp.put("text", user.getUserName());
			userArray.add(userTmp);
		}
		userObj.put("dataList", userArray);
		resultArray.add(userObj);
		//分组
		JSONObject teamObj = new JSONObject();
		teamObj.put("value", "team");
		teamObj.put("text", "分组");
		JSONArray teamArray = new JSONArray();
		for(TeamVo team:teamList) {
			JSONObject teamTmp = new JSONObject();
			teamTmp.put("value", TEAM_HEADER+team.getUuid());
			teamTmp.put("text", team.getName());
			teamArray.add(teamTmp);
		}
		teamObj.put("dataList", teamArray);
		resultArray.add(teamObj);
		//角色
		JSONObject roleObj = new JSONObject();
		roleObj.put("value", "role");
		roleObj.put("text", "角色");
		JSONArray roleArray = new JSONArray();
		for(RoleVo role:roleList) {
			JSONObject roleTmp = new JSONObject();
			roleTmp.put("value", ROLE_HEADER+role.getName());
			roleTmp.put("text", role.getDescription());
			roleArray.add(roleTmp);
		}
		roleObj.put("dataList", roleArray);
		resultArray.add(roleObj);
		//默认
		JSONObject commonObj = new JSONObject();
		commonObj.put("value", "common");
		commonObj.put("text", "公共");
		JSONArray commonArray = new JSONArray();
		if(commonList.size()>0) {
			for(String common : commonList) {
				JSONObject commonTmp = new JSONObject();
				commonTmp.put("value", COMMON_HEADER+common);
				commonTmp.put("text", UserType.getText(common));
				commonArray.add(commonTmp);
			}
		}else {
			for (UserType s : UserType.values()) {
				JSONObject commonTmp = new JSONObject();
				commonTmp.put("value", COMMON_HEADER+s.getValue());
				commonTmp.put("text", s.getText());
				commonArray.add(commonTmp);
			}
		}
		commonObj.put("dataList", commonArray);
		resultArray.add(commonObj);
		
		return resultArray;
	}
	

}
