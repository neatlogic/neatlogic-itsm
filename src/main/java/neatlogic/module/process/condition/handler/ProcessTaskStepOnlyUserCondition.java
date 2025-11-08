/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.*;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepWorkerSqlTable;
import neatlogic.framework.process.workcenter.table.util.SqlTableUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskStepOnlyUserCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    UserMapper userMapper;

    @Override
    public String getName() {
        return "steponlyuser";
    }

    @Override
    public String getDisplayName() {
        return "步骤处理人";
    }

    @Override
    public String getDesc() {
        return "过滤出分派到该用户和该用户正在处理的工单（不包含分派到该用户所以在组或者角色抢单的工单）";
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
        return 7;
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
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        ConditionVo condition = groupVo.getConditionList().get(index);
        List<String> stepUserValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepUserValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepUserValueList.addAll(valueList);
        }
        List<String> userList = new ArrayList<String>();
        for (String user : stepUserValueList) {
            user = user.replace(GroupSearch.USER.getValuePlugin(), "");
            if (user.equals(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue())) {
                user = UserContext.get().getUserUuid();
            }
            userList.add(user);
        }
        sqlSb.append(" (");
        sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskStepWorkerSqlTable().getShortName(), ProcessTaskStepWorkerSqlTable.FieldEnum.UUID.getValue(), String.join("','", userList)));
        sqlSb.append(" ) ");

    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return SqlTableUtil.getWorkerJoinTableSql();
    }
}
