package codedriver.module.process.condition.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.workcenter.dto.JoinOnVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskFocusSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepUserSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProcessTaskAboutMeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Autowired
    UserMapper userMapper;

    private String formHandlerType = FormHandlerType.SELECT.toString();

    private final Map<String, Function<String, String>> map = new HashMap<>();

    private final Map<String, MyProcessTask<StringBuilder>> mapSql = new HashMap<>();

    private final Map<String, MyProcessTask<List<JoinTableColumnVo>>> joinTableSqlMap = new HashMap<>();

    @FunctionalInterface
    public interface MyProcessTask<T> {
        void build(T t);
    }

    {
        map.put("processingOfMine", sql -> getMeWillDoCondition());
        map.put("doneOfMine", sql -> getMeDoneCondition());
        map.put("focusOfMine", sql -> getMyFocusCondition());

        mapSql.put("doneOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStepUserStatus.DONE.getValue()));
            sqlSb.append(" ) and ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.USER_UUID.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) ");
        });
        mapSql.put("focusOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskFocusSqlTable().getShortName(), ProcessTaskFocusSqlTable.FieldEnum.USER_UUID.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) ");
        });

        joinTableSqlMap.put("doneOfMine", (list) -> {
            list.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
            list.add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskStepUserSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue(), ProcessTaskStepUserSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepUserSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue()));
            }}));
        });
        joinTableSqlMap.put("focusOfMine", (list) -> {
            list.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskFocusSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskFocusSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
        });
    }

    @Override
    public String getName() {
        return "aboutme";
    }

    @Override
    public String getDisplayName() {
        return "与我相关";
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        if (FormConditionModel.SIMPLE == processWorkcenterConditionType) {
            formHandlerType = FormHandlerType.RADIO.toString();
        }
        return formHandlerType;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getMyEsName(String... values) {
        String esName = StringUtils.EMPTY;
        if (ArrayUtils.isNotEmpty(values)) {
            switch (values[0]) {
                case "willdo":
                    esName = String.format("%s.%s", getType(), "step.usertypelist.list.status");
                    break;
                case "done":
                    esName = String.format("%s.%s", getType(), "step.usertypelist.list.status");
                    break;
                case "myfocus":
                    esName = String.format("%s.%s", getType(), ProcessWorkcenterField.FOCUS_USERS.getValue());
                    break;

            }
        }
        return esName;
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONArray dataList = new JSONArray();
        dataList.add(new ValueTextVo("doneOfMine", "已办"));
        dataList.add(new ValueTextVo("focusOfMine", "已关注"));

        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", false);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", false);
        return config;
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
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        String where = StringUtils.EMPTY;
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        for (String value : valueList) {
            Function<String, String> result = map.get(value);
            if (result != null) {
                // 拼接条件
                String tmpWhere = result.apply("");
                if (StringUtil.isBlank(where)) {
                    where = tmpWhere;
                } else {
                    if (StringUtils.isNotBlank(tmpWhere)) {
                        // TODO 不支持多选，语法不支持，后续看怎么支持
                        // where = String.format(" %s or %s", where,result.apply(""));
                    }
                }
            }
        }
        return where;
    }

    /**
     * 附加我的待办条件
     *
     * @return
     */
    private String getMeWillDoCondition() {
        String sql = StringUtils.EMPTY;
        // status
        List<String> statusList = Arrays.asList(ProcessTaskStatus.DRAFT.getValue(), ProcessTaskStatus.RUNNING.getValue()).stream()
                .map(object -> object.toString()).collect(Collectors.toList());
        String statusSql = String.format(Expression.INCLUDE.getExpressionEs(),
                ((IProcessTaskCondition) ConditionHandlerFactory.getHandler(ProcessWorkcenterField.STATUS.getValue()))
                        .getEsName(),
                String.format(" '%s' ", String.join("','", statusList)));
        // common.step.filtstatus
        List<String> stepStatusList =
                Arrays.asList(ProcessTaskStatus.DRAFT.getValue(), ProcessTaskStatus.PENDING.getValue(), ProcessTaskStatus.RUNNING.getValue()).stream()
                        .map(object -> object.toString()).collect(Collectors.toList());
        String stepStatusSql = String.format(Expression.INCLUDE.getExpressionEs(),
                ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue()) + ".status",
                String.format(" '%s' ", String.join("','", stepStatusList)));
        // common.step.usertypelist.userlist
        List<String> userList = new ArrayList<String>();
        userList.add(GroupSearch.USER.getValuePlugin() + UserContext.get().getUserUuid());
        // 如果是待处理状态，则需额外匹配角色和组
        UserVo userVo = userMapper.getUserByUuid(UserContext.get().getUserUuid());
        if (userVo != null) {
            List<String> teamList = userVo.getTeamUuidList();
            if (CollectionUtils.isNotEmpty(teamList)) {
                for (String team : teamList) {
                    userList.add(GroupSearch.TEAM.getValuePlugin() + team);
                }
            }
            List<String> roleUuidList = userVo.getRoleUuidList();
            if (CollectionUtils.isNotEmpty(roleUuidList)) {
                for (String roleUuid : roleUuidList) {
                    userList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
                }
            }
        }
        sql = String.format(
                " %s and %s and common.step.usertypelist.list.value contains any ( %s ) and common.step.usertypelist.list.status contains any ('pending','doing') and not common.step.isactive contains any (0,-1)",
                statusSql, stepStatusSql, String.format(" '%s' ", String.join("','", userList)));
        // sql = String.format(" common.step.usertypelist.list.value contains any ( %s ) and
        // common.step.usertypelist.list.status contains any ('pending','doing')", String.format(" '%s' ",
        // String.join("','",userList))) ;
        return sql;
    }

    private String getMeDoneCondition() {
        String sql = StringUtils.EMPTY;
        sql = String.format(
                " common.step.usertypelist.list.value = '%s' and common.step.usertypelist.list.status = 'done'",
                GroupSearch.USER.getValuePlugin() + UserContext.get().getUserUuid());
        return sql;
    }

    private String getMyFocusCondition() {
        String sql = StringUtils.EMPTY;
        sql = String.format(Expression.INCLUDE.getExpressionEs(),
                String.format(" %s.%s", getType(), ProcessWorkcenterField.FOCUS_USERS.getValue()),
                String.format(" '%s' ", GroupSearch.USER.getValuePlugin() + UserContext.get().getUserUuid()));
        return sql;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        sqlSb.append(" ( ");
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        int i = 0;
        for (String value : valueList) {
            MyProcessTask<StringBuilder> result = mapSql.get(value);
            if (result != null) {
                // 拼接条件
                result.build(sqlSb);
                if (i != 0) {
                    sqlSb.append(" or ");
                }
            }
            i++;
        }
        sqlSb.append(" ) ");
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(WorkcenterVo workcenterVo) {
        List<JoinTableColumnVo> joinTableColumnVoList = new ArrayList<>();
        for (ConditionGroupVo conditionGroupVo : workcenterVo.getConditionGroupList()) {
            for (ConditionVo conditionVo : conditionGroupVo.getConditionList()) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(conditionVo.getValueList()), String.class);
                for (String value : valueList) {
                    MyProcessTask<List<JoinTableColumnVo>> result = joinTableSqlMap.get(value);
                    if (result != null) {
                        result.build(joinTableColumnVoList);
                    }
                }
            }
        }
        return joinTableColumnVoList;
    }
}
