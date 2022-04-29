/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.condition.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.*;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepUserSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import codedriver.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskStepUserCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    UserMapper userMapper;

    @Resource
    AuthenticationInfoService authenticationInfoService;

    @Override
    public String getName() {
        return "stepuser";
    }

    @Override
    public String getDisplayName() {
        return "步骤处理人";
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.USERSELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType configType) {
        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.USERSELECT.toString());
        config.put("initConfig", new JSONObject() {
            {
                this.put("excludeList", new JSONArray() {{
                    this.add(GroupSearch.COMMON.getValuePlugin() + UserType.ALL.getValue());
                }});
                this.put("groupList", new JSONArray() {
                    {
                        this.add(GroupSearch.USER.getValue());
                        this.add(GroupSearch.COMMON.getValue());
                    }
                });
                this.put("includeList", new JSONArray() {
                    {
                        this.add(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue());
                    }
                });
            }
        });
        config.put("multiple", true);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 12;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }


    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(value.toString().substring(5));
                if (userVo != null) {
                    return userVo.getUserName();
                } else {
                    if (value.toString().startsWith("common#")) {
                        return UserType.getText(value.toString().substring(7));
                    }
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    UserVo userVo = userMapper.getUserBaseInfoByUuid(valueStr.substring(5));
                    if (userVo != null) {
                        textList.add(userVo.getUserName());
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
        ConditionVo condition = conditionList.get(index);
        List<String> stepUserValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepUserValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepUserValueList.addAll(valueList);
        }
        List<String> userList = new ArrayList<String>();
        List<String> teamList = new ArrayList<String>();
        List<String> roleList = new ArrayList<String>();
        for (String user : stepUserValueList) {
            user = user.replace(GroupSearch.USER.getValuePlugin(), "");
            if (user.equals(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue())) {
                user = UserContext.get().getUserUuid();
            }
            //如果是待处理状态，则需额外匹配角色和组
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(user);
            if (authenticationInfoVo != null) {
                userList.add(user);
                teamList = authenticationInfoVo.getTeamUuidList();
                roleList = authenticationInfoVo.getRoleUuidList();
            }
        }
        sqlSb.append(" (");
        // status
        /*List<String> statusList = Stream.of(ProcessTaskStatus.RUNNING.getValue())
                .map(String::toString).collect(Collectors.toList());
        sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), String.join("','", statusList)));
        sqlSb.append(" and ( ");*/
        //补充待处理人sql 条件
        getProcessingTaskOfMineSqlWhere(sqlSb, userList, teamList, roleList);
        //补充处理人sql 条件
        sqlSb.append(" or ");
        sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.USER_UUID.getValue(), String.join("','", userList)));
        sqlSb.append(" ) ");

    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return SqlTableUtil.getStepUserJoinTableSql();
    }
}
