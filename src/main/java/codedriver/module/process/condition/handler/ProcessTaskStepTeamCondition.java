package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskStepTeamCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
    @Autowired
    TeamMapper teamMapper;
    
    @Autowired
    UserMapper userMapper;
    
    @Autowired
    RoleMapper roleMapper;
    
	@Override
	public String getName() {
		return "stepteam";
	}

	@Override
	public String getDisplayName() {
		return "处理组";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject returnObj = new JSONObject();
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 13;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public String getMyEsName() {
        return String.format(" %s.%s", getType(),"step.usertypelist.userlist");
    }

	@Override
    protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
	    //获取条件
	    ConditionVo condition = conditionList.get(index);
	    List<String> stepTeamValueList = new ArrayList<>();
        if(condition.getValueList() instanceof String) {
            stepTeamValueList.add((String)condition.getValueList());
        }else if(condition.getValueList() instanceof List){
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepTeamValueList.addAll(valueList);
        }
        Set<String> userTypeList = new HashSet<String>();
        List<String> userUuidList = new ArrayList<String>();
        List<String> teamUuidList = new ArrayList<String>();
        //如果存在当前登录人所在组
        String loginTeam = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_TEAM.getValue();
        if(stepTeamValueList.contains(loginTeam)){
            List<TeamVo> teamTmpList = teamMapper.searchTeamByUserUuidAndLevelList(UserContext.get().getUserUuid(true),Arrays.asList(TeamLevel.TEAM.getValue()));
            if(CollectionUtils.isNotEmpty(teamTmpList)) {
                for(TeamVo team :teamTmpList) {
                    stepTeamValueList.add(GroupSearch.TEAM.getValuePlugin()+team.getUuid());
                }
            }
            stepTeamValueList.remove(loginTeam);
        }
        //如果存在当前登录人所在部(寻找当前登录人所在部，并穿透获取所有子组)
        String loginDepartment = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_DEPARTMENT.getValue();
        List<TeamVo> departmentTeamList = new ArrayList<>();
        if(stepTeamValueList.contains(loginDepartment)){
            List<TeamVo> teamTmpList = teamMapper.searchTeamByUserUuidAndLevelList(UserContext.get().getUserUuid(true),Arrays.asList(TeamLevel.TEAM.getValue(),TeamLevel.DEPARTMENT.getValue()));
            departmentTeamList.addAll(teamMapper.getDepartmentTeamUuidByTeamList(teamTmpList));
            if(CollectionUtils.isNotEmpty(departmentTeamList)) {
                List<TeamVo> groupTeamList = teamMapper.getAllSonTeamByParentTeamList(departmentTeamList);
                if(CollectionUtils.isNotEmpty(groupTeamList)) {
                    for(TeamVo groupTeam :groupTeamList) {
                        stepTeamValueList.add(GroupSearch.TEAM.getValuePlugin()+groupTeam.getUuid());
                    }
                }
            }
            stepTeamValueList.remove(loginDepartment);
        }
        userTypeList.addAll(stepTeamValueList);
        //获取所有组的成员
        for(String team : stepTeamValueList) {
            teamUuidList.add(team.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
        }
        if(CollectionUtils.isNotEmpty(teamUuidList)) {
            userUuidList = userMapper.getUserUuidListByTeamUuidList(teamUuidList);
            for(String userUuid : userUuidList) {
                userTypeList.add(GroupSearch.USER.getValuePlugin()+userUuid);
            }
        }
        
        //获取成员角色
        if(CollectionUtils.isNotEmpty(userUuidList)) {
            List<String> roleUuidList = roleMapper.getRoleUuidListByUserUuidList(userUuidList);
            for(String roleUuid : roleUuidList) {
                userTypeList.add(GroupSearch.ROLE.getValuePlugin()+roleUuid);
            }
        }
        String value = String.join("','",userTypeList);
	    return String.format(Expression.INCLUDE.getExpressionEs(),this.getEsName(),String.format("'%s'",  value))+" and not common.step.type = 'start' ";
	}
}
