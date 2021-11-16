package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dto.condition.ConditionGroupRelVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionRelVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexWordOffsetVo;
import codedriver.framework.fulltextindex.utils.FullTextIndexUtil;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessWorkcenterInitType;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.condition.handler.ProcessTaskStartTimeCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: SqlWhereDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:39
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlWhereDecorator extends SqlDecoratorBase {
    private final Map<String, BuildWhere<WorkcenterVo, StringBuilder>> buildWhereMap = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(SqlWhereDecorator.class);

    @FunctionalInterface
    public interface BuildWhere<T, StringBuilder> {
        void build(T t, StringBuilder sqlSb);
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
        buildWhereMap.put(FieldTypeEnum.FIELD.getValue(), (workcenterVo, sqlSb) -> {
            //根据column获取需要的表
            sqlSb.append(String.format(" and %s.%s in ( %s ) ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.ID.getValue(), workcenterVo.getProcessTaskIdList().stream().map(Object::toString).collect(Collectors.joining(","))));
        });

        buildWhereMap.put(FieldTypeEnum.LIMIT_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            getCountWhereSql(sqlSb, workcenterVo);
        });

        buildWhereMap.put(FieldTypeEnum.DISTINCT_ID.getValue(), (workcenterVo, sqlSb) -> {
            getCountWhereSql(sqlSb, workcenterVo);
        });

        buildWhereMap.put(FieldTypeEnum.TOTAL_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            getCountWhereSql(sqlSb, workcenterVo);
        });

        buildWhereMap.put(FieldTypeEnum.FULL_TEXT.getValue(), (workcenterVo, sqlSb) -> {
            JSONArray keywordConditionList = workcenterVo.getKeywordConditionList();
            //获取过滤最终工单idList
            if (CollectionUtils.isNotEmpty(keywordConditionList)) {
                for (Object keywordCondition : keywordConditionList) {
                    JSONObject keywordConditionJson = JSONObject.parseObject(JSONObject.toJSONString(keywordCondition));
                    String handler = keywordConditionJson.getString("name");
                    JSONArray valueArray = keywordConditionJson.getJSONArray("valueList");
                    List<String> valueList = JSONObject.parseArray(valueArray.toJSONString(), String.class);
                    getFullTextSql(sqlSb, String.join("\" \"", valueList), handler);
                }
            } else {//获取关键字搜索下拉选项
                getFullTextSql(sqlSb, workcenterVo.getKeyword(), workcenterVo.getKeywordHandler());
            }
        });

        buildWhereMap.put(FieldTypeEnum.GROUP_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            getCountWhereSql(sqlSb, workcenterVo);
            IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getDashboardConfigVo().getGroup());
            //拼接sql，对二次过滤选项，如：数值图需要二次过滤选项
            JSONObject chartConfig = workcenterVo.getDashboardConfigVo().getChartConfig();
            List<String> groupDataList = new ArrayList<>();
            if (chartConfig.containsKey("configlist")) {
                JSONArray configArray = chartConfig.getJSONArray("configlist");
                groupDataList = JSONObject.parseArray(configArray.toJSONString(), String.class);
            }
            //拼接sql，则根据查出的权重，排序截取最大组数量，查出二维数据
            LinkedHashMap<String, Object> groupDataMap = workcenterVo.getDashboardConfigVo().getGroupDataCountMap();
            if (MapUtils.isNotEmpty(groupDataMap)) {
                for (Map.Entry<String, Object> entry : groupDataMap.entrySet()) {
                    groupDataList.add(entry.getKey());
                }
            }

            if (columnHandler != null){
                List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
                OUT:
                for (TableSelectColumnVo columnVo : columnVoList) {
                    for (SelectColumnVo column : columnVo.getColumnList()) {
                        if (column.getIsPrimary()) {
                            if (CollectionUtils.isNotEmpty(groupDataList)) {
                                sqlSb.append(String.format(" AND %s.`%s` IN ('%s') ", columnVo.getTableShortName(), column.getColumnName(), String.join("','", groupDataList)));
                            }
                            break OUT;
                        }
                    }
                }
            }
        });
    }

    private void getFullTextSql(StringBuilder sqlSb, String value, String handler) {
        IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(handler);
        if (columnHandler != null) {
            List<String> columnList = new ArrayList<>();
            List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
            for (TableSelectColumnVo columnVo : columnVoList) {
                for (SelectColumnVo column : columnVo.getColumnList()) {
                    columnList.add(String.format("%s.%s", columnVo.getTableShortName(), column.getColumnName()));
                }
            }
            sqlSb.append(String.format(" AND MATCH (%s)  AGAINST ('\"%s\"' IN BOOLEAN MODE) ", String.join(",", columnList), value));
        }
    }

    /**
     * @Description: count , distinct id 则 需要 根据条件获取需要的表。
     * @Author: 89770
     * @Date: 2021/2/8 16:33
     * @Params: [sqlSb, workcenterVo]
     * @Returns: void
     **/
    private void getCountWhereSql(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        // 将group 以连接表达式 存 Map<fromUuid_toUuid,joinType>
        Map<String, String> groupRelMap = new HashMap<String, String>();
        List<ConditionGroupRelVo> groupRelList = workcenterVo.getConditionGroupRelList();
        if (CollectionUtils.isNotEmpty(groupRelList)) {
            for (ConditionGroupRelVo groupRel : groupRelList) {
                groupRelMap.put(groupRel.getFrom() + "_" + groupRel.getTo(), groupRel.getJoinType());
            }
        }
        List<ISqlTable> tableList = new ArrayList<>();
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        if (CollectionUtils.isNotEmpty(groupList)) {
            String fromGroupUuid = null;
            String toGroupUuid = groupList.get(0).getUuid();
            boolean isAddedAnd = false;
            for (ConditionGroupVo groupVo : groupList) {
                // 将condition 以连接表达式 存 Map<fromUuid_toUuid,joinType>
                Map<String, String> conditionRelMap = new HashMap<String, String>();
                List<ConditionRelVo> conditionRelList = groupVo.getConditionRelList();
                if (CollectionUtils.isNotEmpty(conditionRelList)) {
                    for (ConditionRelVo conditionRel : conditionRelList) {
                        conditionRelMap.put(conditionRel.getFrom() + "_" + conditionRel.getTo(),
                                conditionRel.getJoinType());
                    }
                }
                //append joinType
                if (fromGroupUuid != null) {
                    toGroupUuid = groupVo.getUuid();
                    sqlSb.append(groupRelMap.get(fromGroupUuid + "_" + toGroupUuid));
                }
                List<ConditionVo> conditionVoList = groupVo.getConditionList();
                if (!isAddedAnd && CollectionUtils.isNotEmpty((conditionVoList))) {
                    sqlSb.append(" and ");
                    isAddedAnd = true;
                }
                sqlSb.append(" ( ");
                String fromConditionUuid = null;
                String toConditionUuid = null;
                for (int i = 0; i < conditionVoList.size(); i++) {
                    ConditionVo conditionVo = conditionVoList.get(i);
                    //append joinType
                    toConditionUuid = conditionVo.getUuid();
                    if (MapUtils.isNotEmpty(conditionRelMap) && StringUtils.isNotBlank(fromConditionUuid)) {
                        sqlSb.append(conditionRelMap.get(fromConditionUuid + "_" + toConditionUuid));
                    }
                    //append condition
                    String handler = conditionVo.getName();
                    //如果是form
                    if (conditionVo.getType().equals(ProcessFieldType.FORM.getValue())) {
                        handler = ProcessFieldType.FORM.getValue();
                    }
                    IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(handler);
                    sqlCondition.getSqlConditionWhere(conditionVoList, i, sqlSb);
                    fromConditionUuid = toConditionUuid;
                }
                sqlSb.append(" ) ");
                fromGroupUuid = toGroupUuid;

            }
        }
    }

    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" where ");
        //上报时间
        ProcessTaskStartTimeCondition startTimeSqlCondition = (ProcessTaskStartTimeCondition) ConditionHandlerFactory.getHandler("starttime");
        startTimeSqlCondition.getDateSqlWhere(workcenterVo.getStartTimeCondition(), sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
        //我的待办
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            sqlSb.append(" and ");
            IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler("processingofmine");
            sqlCondition.getSqlConditionWhere(null, 0, sqlSb);
        }
        //keyword搜索框搜索 idList 过滤
        if (CollectionUtils.isNotEmpty(workcenterVo.getKeywordConditionList())) {
            for(Object obj : workcenterVo.getKeywordConditionList()){
                JSONObject condition = JSONObject.parseObject(obj.toString());
                //title serialNumber 全词匹配
                if(ProcessTaskSqlTable.FieldEnum.TITLE.getValue().equals(condition.getString("name"))){
                    sqlSb.append(String.format(" AND pt.title in ('%s') ", condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining("','"))));
                }else if(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getValue().equals(condition.getString("name"))){
                    sqlSb.append(String.format(" AND pt.serial_number in (%s) ", condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining(","))));
                }else {
                    try {
                        List<FullTextIndexWordOffsetVo> wordOffsetVoList = FullTextIndexUtil.sliceWord(condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining("")));
                        String contentWord = wordOffsetVoList.stream().map(FullTextIndexWordOffsetVo::getWord).collect(Collectors.joining("','"));
                        sqlSb.append(String.format("  AND EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s' AND fw.word IN ('%s') )", condition.getString("name"), contentWord));
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        //隐藏工单 过滤
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        if (!isHasProcessTaskAuth) {
            sqlSb.append(" and pt.is_show = 1 ");
        }
        //其它条件过滤
        buildWhereMap.get(workcenterVo.getSqlFieldType()).build(workcenterVo, sqlSb);
        //只有”我的草稿“分类才显示工单状态”未提交“的工单
        if(!Objects.equals(ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue(),workcenterVo.getUuid()) && !sqlSb.toString().contains("draft")&&Arrays.asList(FieldTypeEnum.DISTINCT_ID.getValue(),FieldTypeEnum.TOTAL_COUNT.getValue(),FieldTypeEnum.LIMIT_COUNT.getValue()).contains(workcenterVo.getSqlFieldType())){
            sqlSb.append(" and pt.status != 'draft' ");
        }

    }

    @Override
    public int getSort() {
        return 4;
    }
}
