package codedriver.module.process.api.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javers.common.collections.Arrays;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.mchange.lang.ArrayUtils;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.workerdispatcher.core.IWorkerDispatcher;
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
		@Param(name = "groupList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "限制接口返回类型，['common','user','team','role']"),
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
		jsonObj.put("userMapper", userMapper);
		jsonObj.put("roleMapper", roleMapper);
		jsonObj.put("teamMapper", teamMapper);
		List<Object> groupList = null;
		if(jsonObj.containsKey("groupList")) {
			groupList = Arrays.asList(jsonObj.getJSONArray("groupList").toArray());
		}
		Reflections reflections = new Reflections(this.getClass().getPackage().getName());
		Set<?> apiClass = reflections.getSubTypesOf(IHandler.class);
		JSONArray resultArray = new JSONArray();
		for (Object c: apiClass) {
			IHandler handler = (IHandler) ((Class<?>) c).getConstructors()[0].newInstance(UserRoleTeamSearchApi.class.newInstance());
			if(groupList != null && !groupList.contains(handler.getName())) {
				continue;
			}
			List<Object> dataList = null;
			if(jsonObj.containsKey("keyword")) {
				dataList = handler.search(jsonObj);
			}else {
				if(jsonObj.containsKey("valueList") && !jsonObj.getJSONArray("valueList").isEmpty()) {
					dataList = handler.reload(jsonObj);
				}
			}
			resultArray.add(handler.repack(dataList));
		}
		//排序
		resultArray.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("sort")));
		return resultArray;
	}
	
	public interface IHandler{
		String getName();
		
		String getHeader();
		
		int getSort();
		
		<T> List<T> search(JSONObject jsonObj);
		
		<T> List<T> reload(JSONObject jsonObj);
		
		<T> JSONObject repack(List<T> dataList);
		
		
	}
	
    public class RoleHandler implements IHandler{
		@Override
		public String getName() {
    		return "role";
    	}
    	
    	@Override
    	public String getHeader() {
    		return getName()+"#";
    	}


		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> search(JSONObject jsonObj) {
			List<RoleVo> roleList = new ArrayList<RoleVo>();
			RoleVo roleVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<RoleVo>() {});
			if(roleVo.getNeedPage()) {
				int rowNum = ((RoleMapper)jsonObj.get("roleMapper")).searchRoleCount(roleVo);
				roleVo.setPageCount(PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
				roleVo.setRowNum(rowNum);
			}
			roleList = ((RoleMapper)jsonObj.get("roleMapper")).searchRole(roleVo);
			return (List<T>) roleList;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> reload(JSONObject jsonObj) {
			List<RoleVo> roleList = new ArrayList<RoleVo>();
			List<String> roleNameList = new ArrayList<String>();
			for(Object value :jsonObj.getJSONArray("valueList")) {
				if(value.toString().startsWith(getHeader())){
					roleNameList.add(value.toString().replace(getHeader(), ""));
				}
			}
			if(roleNameList.size()>0) {
				roleList = ((RoleMapper)jsonObj.get("roleMapper")).getRoleByRoleNameList(roleNameList);
			}
			return (List<T>) roleList;
		}

		@Override
		public <T> JSONObject repack(List<T> roleList) {
			JSONObject roleObj = new JSONObject();
			roleObj.put("value", "role");
			roleObj.put("text", "角色");
			JSONArray roleArray = new JSONArray();
			for(T role:roleList) {
				JSONObject roleTmp = new JSONObject();
				roleTmp.put("value", getHeader()+((RoleVo) role).getName());
				roleTmp.put("text", ((RoleVo) role).getDescription());
				roleArray.add(roleTmp);
			}
			roleObj.put("sort", getSort());
			roleObj.put("dataList", roleArray);
			return roleObj;
		}

		@Override
		public int getSort() {
			return 3;
		}

    }
    
    public class UserHandler implements IHandler{
    	@Override
		public String getName() {
    		return "user";
    	}
    	
    	@Override
    	public String getHeader() {
    		return getName()+"#";
    	}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> search(JSONObject jsonObj) {
			List<UserVo> userList = new ArrayList<UserVo>();
			UserVo userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>() {});
			if (userVo.getNeedPage()) {
				int rowNum = ((UserMapper)jsonObj.get("userMapper")).searchUserCount(userVo);
				userVo.setRowNum(rowNum);
				userVo.setPageCount(PageUtil.getPageCount(rowNum, userVo.getPageSize()));
			}
			userList = ((UserMapper)jsonObj.get("userMapper")).searchUser(userVo);
			return (List<T>) userList;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> reload(JSONObject jsonObj) {
			List<UserVo> userList = new ArrayList<UserVo>();
			List<String> userIdList = new ArrayList<String>();
			for(Object value :jsonObj.getJSONArray("valueList")) {
				if(value.toString().startsWith(getHeader())) {
					userIdList.add(value.toString().replace(getHeader(), ""));
				}
			}
			if(userIdList.size()>0) {
				userList = ((UserMapper)jsonObj.get("userMapper")).getUserByUserIdList(userIdList);
			}
			return (List<T>) userList;
		}

		@Override
		public <T> JSONObject repack(List<T> userList) {
			JSONObject userObj = new JSONObject();
			userObj.put("value", "user");
			userObj.put("text", "用户");
			JSONArray userArray = new JSONArray();
			for(T user:userList) {
				JSONObject userTmp = new JSONObject();
				userTmp.put("value", getHeader()+((UserVo) user).getUserId());
				userTmp.put("text", ((UserVo) user).getUserName());
				userArray.add(userTmp);
			}
			userObj.put("sort", getSort());
			userObj.put("dataList", userArray);
			return userObj;
		}

		@Override
		public int getSort() {
			return 1;
		}
    }

    public class TeamHandler implements IHandler{
    	@Override
		public String getName() {
    		return "team";
    	}
    	
    	@Override
    	public String getHeader() {
    		return getName()+"#";
    	}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> search(JSONObject jsonObj) {
			List<TeamVo> teamList = new ArrayList<TeamVo>();
			TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});		
			if (teamVo.getNeedPage()) {
				int rowNum = ((TeamMapper)jsonObj.get("teamMapper")).searchTeamCount(teamVo);
				teamVo.setRowNum(rowNum);
				teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
			}
			teamList = ((TeamMapper)jsonObj.get("teamMapper")).searchTeam(teamVo);
			return (List<T>) teamList;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> reload(JSONObject jsonObj) {
			List<TeamVo> teamList = new ArrayList<TeamVo>();
			List<String> teamUuidList = new ArrayList<String>();
			for(Object value :jsonObj.getJSONArray("valueList")) {
				if(value.toString().startsWith(getHeader())){
					teamUuidList.add(value.toString().replace(getHeader(), ""));
				}
			}
			if(teamUuidList.size()>0) {
				teamList = ((TeamMapper)jsonObj.get("teamMapper")).getTeamByUuidList(teamUuidList);
			}
			return (List<T>) teamList;
		}

		@Override
		public <T> JSONObject repack(List<T> teamList) {
			JSONObject teamObj = new JSONObject();
			teamObj.put("value", "team");
			teamObj.put("text", "分组");
			JSONArray teamArray = new JSONArray();
			for(T team:teamList) {
				JSONObject teamTmp = new JSONObject();
				teamTmp.put("value", getHeader()+((TeamVo) team).getUuid());
				teamTmp.put("text", ((TeamVo) team).getName());
				teamArray.add(teamTmp);
			}
			teamObj.put("sort", getSort());
			teamObj.put("dataList", teamArray);
			return teamObj;
		}

		@Override
		public int getSort() {
			return 2;
		}
    }

    public class CommonHandler implements IHandler{
    	@Override
		public String getName() {
    		return "common";
    	}
    	
    	@Override
    	public String getHeader() {
    		return getName()+"#";
    	}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> search(JSONObject jsonObj) {
			List<String> commonList = new ArrayList<String>();
			for (UserType s : UserType.values()) {
				commonList.add(s.getValue());
			}
			return (List<T>) commonList;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> reload(JSONObject jsonObj) {
			List<String> commonList = new ArrayList<String>();
			for(Object value :jsonObj.getJSONArray("valueList")) {
				if(value.toString().startsWith(getHeader())){
					commonList.add(value.toString().replace(getHeader(), ""));
				}
			}
			return (List<T>) commonList;
		}

		@Override
		public <T> JSONObject repack(List<T> commonList) {
			JSONObject commonObj = new JSONObject();
			commonObj.put("value", "common");
			commonObj.put("text", "公共");
			JSONArray commonArray = new JSONArray();
			for(T common : commonList) {
				JSONObject commonTmp = new JSONObject();
				commonTmp.put("value", getHeader()+common);
				commonTmp.put("text", UserType.getText(common.toString()));
				commonArray.add(commonTmp);
			}
			commonObj.put("sort", getSort());
			commonObj.put("dataList", commonArray);
			return commonObj;
		}

		@Override
		public int getSort() {
			return 0;
		}
    }
}
