/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.condition.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.*;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepUserSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepWorkerSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStepTeamCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
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
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.TEAMSELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType configType) {
        JSONObject returnObj = new JSONObject();
        returnObj.put("type", FormHandlerType.USERSELECT.toString());
        returnObj.put("multiple", true);
        returnObj.put("isMultiple", true);
        returnObj.put("initConfig", new JSONObject() {
            {
                this.put("excludeList", new JSONArray() {{
                    if (ConditionConfigType.WORKCENTER.getValue().equals(configType.getValue())) {
                        this.add(GroupSearch.COMMON.getValuePlugin() + UserType.ALL.getValue());
                    }
                }});
                this.put("groupList", new JSONArray() {
                    {
                        this.add(GroupSearch.TEAM.getValue());
                        this.add(GroupSearch.COMMON.getValue());
                    }
                });
                this.put("includeList", new JSONArray() {
                    {
                        if (ConditionConfigType.WORKCENTER.getValue().equals(configType.getValue())) {
                            this.add(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_TEAM.getValue());
                            this.add(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_DEPARTMENT.getValue());
                        }
                    }
                });
            }
        });
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
        if (value != null) {
            if (value instanceof String) {
                TeamVo teamVo = teamMapper.getTeamByUuid(value.toString().substring(5));
                if (teamVo != null) {
                    return teamVo.getName();
                } else {
                    if (value.toString().startsWith("common#")) {
                        return UserType.getText(value.toString().substring(7));
                    }
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    TeamVo teamVo = teamMapper.getTeamByUuid(valueStr.substring(5));
                    if (teamVo != null) {
                        textList.add(teamVo.getName());
                    } else {
                        if (valueStr.startsWith("common#")) {
                            textList.add(UserType.getText(valueStr.substring(7)));
                        } else {
                            textList.add(valueStr);
                        }
                    }
                }
                return String.join("、", textList);
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        //获取条件
        ConditionVo condition = conditionList.get(index);
        List<String> stepTeamValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepTeamValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepTeamValueList.addAll(valueList);
        }
        Set<String> userTypeList = new HashSet<String>();
        List<String> userUuidList = new ArrayList<String>();
        List<String> teamUuidList = new ArrayList<String>();
        List<String> roleUuidList = new ArrayList<String>();
        List<String> workUuidList = new ArrayList<String>();

        //如果存在当前登录人所在组
        String loginTeam = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_TEAM.getValue();
        if (stepTeamValueList.contains(loginTeam)) {
            List<TeamVo> teamTmpList = teamMapper.searchTeamByUserUuidAndLevelList(UserContext.get().getUserUuid(true), Collections.singletonList(TeamLevel.TEAM.getValue()));
            if (CollectionUtils.isNotEmpty(teamTmpList)) {
                for (TeamVo team : teamTmpList) {
                    stepTeamValueList.add(team.getUuid());
                }
            }
            stepTeamValueList.remove(loginTeam);
        }
        //如果存在当前登录人所在部(寻找当前登录人所在部，并穿透获取所有子组)
        String loginDepartment = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_DEPARTMENT.getValue();
        List<TeamVo> departmentTeamList = new ArrayList<>();
        if (stepTeamValueList.contains(loginDepartment)) {
            List<TeamVo> teamTmpList = teamMapper.searchTeamByUserUuidAndLevelList(UserContext.get().getUserUuid(true), Arrays.asList(TeamLevel.TEAM.getValue(), TeamLevel.DEPARTMENT.getValue()));
            if (CollectionUtils.isNotEmpty(teamTmpList)) {
                departmentTeamList.addAll(teamMapper.getDepartmentTeamUuidByTeamList(teamTmpList));
                if (CollectionUtils.isNotEmpty(departmentTeamList)) {
                    List<TeamVo> groupTeamList = teamMapper.getAllSonTeamByParentTeamList(departmentTeamList);
                    if (CollectionUtils.isNotEmpty(groupTeamList)) {
                        for (TeamVo groupTeam : groupTeamList) {
                            stepTeamValueList.add(groupTeam.getUuid());
                        }
                    }
                }
            }
            stepTeamValueList.remove(loginDepartment);
        }

        //获取所有组的成员
        for (String team : stepTeamValueList) {
            teamUuidList.add(team.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
        }
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            userUuidList = userMapper.getUserUuidListByTeamUuidList(teamUuidList);
            userTypeList.addAll(userUuidList);
        }

        //获取成员角色
        if (CollectionUtils.isNotEmpty(userUuidList)) {
            roleUuidList.addAll(roleMapper.getRoleUuidListByUserUuidList(userUuidList));
        }
        //
        workUuidList.addAll(userUuidList);
        workUuidList.addAll(teamUuidList);
        workUuidList.addAll(roleUuidList);

        sqlSb.append(" (");
        //非开始节点
        sqlSb.append(Expression.getExpressionSql(Expression.UNEQUAL.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.TYPE.getValue(), ProcessStepHandlerType.START.getHandler()));
        sqlSb.append(" and (");
        sqlSb.append(Expression.getExpressionSql(condition.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.USER_UUID.getValue(), String.join("','", userTypeList)));
        sqlSb.append(" or ");
        sqlSb.append(Expression.getExpressionSql(condition.getExpression(), new ProcessTaskStepWorkerSqlTable().getShortName(), ProcessTaskStepWorkerSqlTable.FieldEnum.UUID.getValue(), String.join("','", workUuidList)));
        sqlSb.append(" )) ");
//        return String.format(Expression.INCLUDE.getExpressionEs(),this.getEsName(),String.format("'%s'",  value))+" and not common.step.type = 'start' ";
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return SqlTableUtil.getStepUserJoinTableSql();
    }
}
