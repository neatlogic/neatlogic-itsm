package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: SqlColumnDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:38
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlFromJoinDecorator extends SqlDecoratorBase {

    private final Map<String, SqlFromJoinDecorator.BuildFromJoin<WorkcenterVo, List<String>, List<JoinTableColumnVo>>> buildFromJoinMap = new HashMap<>();

    @FunctionalInterface
    public interface BuildFromJoin<T, List, joinTableColumnList> {
        void build(T t, List joinTableKeyList, joinTableColumnList joinTableColumnList);
    }

    /**
     * @Description: 初始化构建sql where
     * @Author: 89770
     * @Date: 2021/1/19 14:35
     * @Params: []
     * @Returns: void
     **/
    @PostConstruct
    public void fieldDispatcherInit() {
        //根据column获取需要的表
        buildFromJoinMap.put(FieldTypeEnum.FIELD.getValue(), this::getJoinTableOfColumn);

        buildFromJoinMap.put(FieldTypeEnum.LIMIT_COUNT.getValue(),this::getJoinTableOfCondition);
        //如果是distinct id 则 只需要 根据条件获取需要的表
        buildFromJoinMap.put(FieldTypeEnum.DISTINCT_ID.getValue(),this::getJoinTableOfCondition);

        buildFromJoinMap.put(FieldTypeEnum.TOTAL_COUNT.getValue(),this::getJoinTableOfCondition);

        buildFromJoinMap.put(FieldTypeEnum.FULL_TEXT.getValue(),this::getJoinTableOfCondition);

        buildFromJoinMap.put(FieldTypeEnum.GROUP_COUNT.getValue(),this::getJoinTableOfGroupColumn);
    }

    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        List<String> joinTableKeyList = new ArrayList<>();
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        if(buildFromJoinMap.containsKey(workcenterVo.getSqlFieldType())){
            buildFromJoinMap.get(workcenterVo.getSqlFieldType()).build(workcenterVo,joinTableKeyList,joinTableColumnList);
        }
        //补充排序需要的表
        getJoinTableOfOrder(workcenterVo, joinTableKeyList, joinTableColumnList);
        sqlSb.append(" from  processtask pt ");
        for (JoinTableColumnVo joinTableColumn : joinTableColumnList) {
            sqlSb.append(joinTableColumn.toSqlString());
        }
    }

    /**
     * @Description: 补充排序需要 join 的表
     * @Author: 89770
     * @Date: 2021/1/26 20:36
     * @Params: [workcenterVo, joinTableKeyList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.JoinTableColumnVo>
     **/
    private void getJoinTableOfOrder(WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        JSONArray sortJsonArray = workcenterVo.getSortList();
        if (CollectionUtils.isNotEmpty(sortJsonArray)) {
            for (Object sortObj : sortJsonArray) {
                JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                    String handler = entry.getKey();
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(handler);
                    if (column != null && column.getIsSort()) {
                        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
                        for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                            String key = handlerJoinTableColumn.getHash();
                            if (!joinTableKeyList.contains(handlerJoinTableColumn.getHash())) {
                                joinTableColumnList.add(handlerJoinTableColumn);
                                joinTableKeyList.add(key);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @Description: 如果是distinct id 则 只需要 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    private void getJoinTableOfCondition(WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //根据接口入参的返回需要的conditionList,然后获取需要关联的tableList
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        for (ConditionGroupVo groupVo : groupList) {
            List<ConditionVo> conditionVoList = groupVo.getConditionList();
            for (ConditionVo conditionVo : conditionVoList) {
                IProcessTaskCondition conditionHandler = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(conditionVo.getName());
                if (conditionHandler != null) {
                    List<JoinTableColumnVo> handlerJoinTableColumnList = conditionHandler.getJoinTableColumnList(workcenterVo);
                    for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                        String key = handlerJoinTableColumn.getHash();
                        if (!joinTableKeyList.contains(key)) {
                            joinTableColumnList.add(handlerJoinTableColumn);
                            joinTableKeyList.add(key);
                        }
                    }
                }
            }
        }
        //我的待办 条件
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            List<JoinTableColumnVo> handlerJoinTableColumnList = SqlTableUtil.getProcessingOfMineJoinTableSql();
            for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                String key = handlerJoinTableColumn.getHash();
                if (!joinTableKeyList.contains(key)) {
                    joinTableColumnList.add(handlerJoinTableColumn);
                    joinTableKeyList.add(key);
                }
            }
        }
    }

    private void getJoinTableOfColumn(WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        if (CollectionUtils.isNotEmpty(workcenterVo.getTheadVoList())) {
            for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
                //去掉沒有勾选的thead
                if (theadVo.getIsShow() != 1) {
                    continue;
                }
                if (columnComponentMap.containsKey(theadVo.getName())) {
                    getJoinTableColumnList(columnComponentMap, theadVo.getName(), joinTableKeyList, joinTableColumnList);
                }
            }
        }
    }

    private void getJoinTableOfGroupColumn(WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        List<String> groupList = new ArrayList<>();
        if (StringUtils.isNotBlank(workcenterVo.getDashboardConfigVo().getGroup())) {
            groupList.add(workcenterVo.getDashboardConfigVo().getGroup());
        }
        if (StringUtils.isNotBlank(workcenterVo.getDashboardConfigVo().getSubGroup())) {
            groupList.add(workcenterVo.getDashboardConfigVo().getSubGroup());
        }
        if (CollectionUtils.isNotEmpty(groupList)) {//group by 需要join的表
            for (String group : groupList) {
                getJoinTableColumnList(columnComponentMap, group, joinTableKeyList, joinTableColumnList);
            }
        }
    }

    private void getJoinTableColumnList(Map<String, IProcessTaskColumn> columnComponentMap, String columnName, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        IProcessTaskColumn column = columnComponentMap.get(columnName);
        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
        for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
            String key = handlerJoinTableColumn.getHash();
            if (!joinTableKeyList.contains(key)) {
                joinTableColumnList.add(handlerJoinTableColumn);
                joinTableKeyList.add(key);
            }
        }
    }

    @Override
    public int getSort() {
        return 3;
    }

}
