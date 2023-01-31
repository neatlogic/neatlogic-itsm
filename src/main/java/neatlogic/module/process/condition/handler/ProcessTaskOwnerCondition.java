/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.condition.handler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.*;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessTaskOwnerCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getName() {
        return "owner";
    }

    @Override
    public String getDisplayName() {
        return "上报人";
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
        config.put("multiple", true);
        config.put("initConfig", new JSONObject() {
            {
                this.put("excludeList", new JSONArray() {{
                    if (ConditionConfigType.WORKCENTER.getValue().equals(configType.getValue())) {
                        this.add(GroupSearch.COMMON.getValuePlugin() + UserType.ALL.getValue());
                    }
                }});
                this.put("groupList", new JSONArray() {
                    {
                        if (ConditionConfigType.WORKCENTER.getValue().equals(configType.getValue())) {
                            this.add(GroupSearch.COMMON.getValue());
                        }
                        this.add(GroupSearch.USER.getValue());
                    }
                });
                this.put("includeList", new JSONArray() {
                    {
                        if (ConditionConfigType.WORKCENTER.getValue().equals(configType.getValue())) {
                            this.add(GroupSearch.COMMON.getValuePlugin() + UserType.VIP_USER.getValue());
                            this.add(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue());
                        }
                    }
                });
            }
        });
        /* 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 4;
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
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        //替换“当前登录人标识”为当前登录用户
        String loginUser = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue();
        String vipUser = GroupSearch.COMMON.getValuePlugin() + UserType.VIP_USER.getValue();
        if (valueList.contains(loginUser) || valueList.contains(vipUser)) {
            Iterator<String> valueIterator = valueList.iterator();
            if (valueIterator.hasNext()) {
                String value = valueIterator.next();
                if (value.equals(loginUser)) {
                    valueIterator.remove();
                    valueList.add(UserContext.get().getUserUuid());
                } else if (value.equals(vipUser)) {
                    valueIterator.remove();
                    List<UserVo> userVoList = userMapper.getUserVip();
                    if (CollectionUtils.isNotEmpty(userVoList)) {
                        for (UserVo user : userVoList) {
                            valueList.add(user.getUuid());
                        }
                    }
                }
            }
        }
        String value = valueList.stream().map(o -> o.replaceAll(GroupSearch.USER.getValuePlugin(), "")).collect(Collectors.joining("','"));
        sqlSb.append(Expression.getExpressionSql(condition.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.OWNER.getValue(), value.toString()));
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        return processTaskVo.getOwner();
    }

}
