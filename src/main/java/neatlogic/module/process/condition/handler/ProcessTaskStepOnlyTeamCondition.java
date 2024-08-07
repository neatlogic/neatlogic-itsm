/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.*;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepWorkerSqlTable;
import neatlogic.framework.process.workcenter.table.util.SqlTableUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskStepOnlyTeamCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    TeamMapper teamMapper;

    @Override
    public String getName() {
        return "steponlyteam";
    }

    @Override
    public String getDisplayName() {
        return "步骤处理组";
    }

    @Override
    public String getDesc() {
        return "过滤出分派到该组的工单（指定抢单包含该组的工单）";
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
                        }
                    }
                });
            }
        });
        return returnObj;
    }

    @Override
    public Integer getSort() {
        return 8;
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
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        //获取条件
        ConditionVo condition = groupVo.getConditionList().get(index);
        List<String> stepTeamValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepTeamValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepTeamValueList.addAll(valueList);
        }
        List<String> teamUuidList = new ArrayList<String>();

        //如果存在当前登录人所在组
        String loginTeam = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_TEAM.getValue();
        if (stepTeamValueList.contains(loginTeam)) {
            List<TeamVo> teamTmpList = teamMapper.getTeamListByUserUuid(UserContext.get().getUserUuid(true));
            if (CollectionUtils.isNotEmpty(teamTmpList)) {
                for (TeamVo team : teamTmpList) {
                    stepTeamValueList.add(team.getUuid());
                }
            }
            stepTeamValueList.remove(loginTeam);
        }

        //获取所有组的成员
        for (String team : stepTeamValueList) {
            teamUuidList.add(team.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
        }



        sqlSb.append(" (");
        //非开始节点
        sqlSb.append(Expression.getExpressionSql(Expression.UNEQUAL.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.TYPE.getValue(), ProcessStepHandlerType.START.getHandler()));
        sqlSb.append(" and (");
        sqlSb.append(Expression.getExpressionSql(condition.getExpression(), new ProcessTaskStepWorkerSqlTable().getShortName(), ProcessTaskStepWorkerSqlTable.FieldEnum.UUID.getValue(), String.join("','", teamUuidList)));
        sqlSb.append(" )) ");

    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return SqlTableUtil.getWorkerJoinTableSql();
    }
}
