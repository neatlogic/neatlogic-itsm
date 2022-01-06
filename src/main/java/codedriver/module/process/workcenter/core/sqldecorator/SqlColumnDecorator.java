package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import com.alibaba.fastjson.JSONArray;
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
public class SqlColumnDecorator extends SqlDecoratorBase {

    private final Map<String, BuildField<WorkcenterVo, StringBuilder>> buildFieldMap = new HashMap<>();

    /**
     * @Description: 初始化构建sql field
     * @Author: 89770
     * @Date: 2021/1/19 14:35
     * @Params: []
     * @Returns: void
     **/
    @PostConstruct
    public void fieldDispatcherInit() {
        buildFieldMap.put(FieldTypeEnum.DISTINCT_ID.getValue(), (workcenterVo, sqlSb) -> {
            ProcessTaskSqlTable processTaskSqlTable = new ProcessTaskSqlTable();

            sqlSb.append(String.format(" %s.%s ", processTaskSqlTable.getShortName(), "id"));
            //拼接排序字段
            /*JSONArray sortList = workcenterVo.getSortList();
            if (CollectionUtils.isNotEmpty(sortList)) {
                for (Object sortObj : sortList) {
                    JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                    for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                        String key = entry.getKey();
                        IProcessTaskColumn processTaskColumn = ProcessTaskColumnFactory.getHandler(key);
                        if (processTaskColumn != null && processTaskColumn.getIsSort()) {
                            sqlSb.append(String.format(",%s", processTaskColumn.getSortSqlColumn(true)));
                        }
                    }
                }
            } else {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue());
                sqlSb.append(String.format(",%s", column.getSortSqlColumn(true)));
            }*/
        });

        buildFieldMap.put(FieldTypeEnum.TOTAL_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            sqlSb.append(" COUNT( distinct pt.id ) ");
        });

        buildFieldMap.put(FieldTypeEnum.LIMIT_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            sqlSb.append(" distinct pt.id ");
        });

        buildFieldMap.put(FieldTypeEnum.FIELD.getValue(), (workcenterVo, sqlSb) -> {
            buildField(sqlSb, workcenterVo);
        });

        buildFieldMap.put(FieldTypeEnum.FULL_TEXT.getValue(), (workcenterVo, sqlSb) -> {
            JSONArray keywordConditionList = workcenterVo.getKeywordConditionList();
            if(CollectionUtils.isNotEmpty(keywordConditionList)){
                sqlSb.append(" pt.id  ");
            }else {
                //获取关键字
                IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getKeywordHandler());
                if (columnHandler != null) {
                    List<String> matchColumnList = new ArrayList<>();
                    List<String> columnList = new ArrayList<>();
                    List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
                    for (TableSelectColumnVo columnVo : columnVoList) {
                        for (SelectColumnVo column : columnVo.getColumnList()) {
                            matchColumnList.add(String.format("%s.%s", columnVo.getTableShortName(), column.getColumnName()));
                            columnList.add(String.format("%s.%s as %s", columnVo.getTableShortName(), column.getColumnName(), column.getPropertyName()));
                        }
                    }
                    sqlSb.append(String.format(" pt.id , %s ,MATCH (%s)  AGAINST ('\"%s\"' IN BOOLEAN MODE) AS score  ", String.join(",", columnList), String.join(",", matchColumnList), workcenterVo.getKeyword()));
                }
            }
        });

        buildFieldMap.put(FieldTypeEnum.GROUP_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
            List<String> columnList = new ArrayList<>();
            getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardConfigVo().getGroup());
            if(StringUtils.isNotBlank(workcenterVo.getDashboardConfigVo().getSubGroup())){
                getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardConfigVo().getSubGroup());
            }
            columnList.add("count(1) `count`");
            sqlSb.append(String.join(",", columnList));
        });
    }

    /**
     * @Description: 获取column sql list
     * @Author: 89770
     * @Date: 2021/2/26 17:53
     * @Params: [columnComponentMap, columnList, theadVo]
     * @Returns: void
     **/
    private void getColumnSqlList(Map<String, IProcessTaskColumn> columnComponentMap, List<String> columnList, String theadName) {
        if (columnComponentMap.containsKey(theadName)) {
            IProcessTaskColumn column = columnComponentMap.get(theadName);
            for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                    //TODO  如果是mariadb 则不支持 any_value 高级函数
                    String columnStr = String.format(" any_value(%s.%s) as %s ", tableSelectColumnVo.getTableShortName(), selectColumnVo.getColumnName(), selectColumnVo.getPropertyName());
                    if (!columnList.contains(columnStr)) {
                        columnList.add(columnStr);
                    }
                }
            }

        }
    }

    @FunctionalInterface
    public interface BuildField<T, StringBuilder> {
        void build(T t, StringBuilder sqlSb);
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
        buildFieldMap.get(workcenterVo.getSqlFieldType()).build(workcenterVo, sqlSb);
    }

    /**
     * @Description: 构造column sql
     * @Author: 89770
     * @Date: 2021/1/19 16:32
     * @Params: [sqlSb, workcenterVo]
     * @Returns: void
     **/
    private void buildField(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
            //去掉沒有勾选的thead
            if (theadVo.getIsShow() != 1) {
                continue;
            }
            getColumnSqlList(columnComponentMap, columnList, theadVo.getName());
        }
        //查询是否隐藏
        columnList.add(String.format(" %s.%s as %s ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.IS_SHOW.getValue(), ProcessTaskSqlTable.FieldEnum.IS_SHOW.getValue()));
        columnList.add(String.format(" %s.%s as %s ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskSqlTable.FieldEnum.ID.getValue()));
        sqlSb.append(String.join(",", columnList));
    }

    @Override
    public int getSort() {
        return 2;
    }

}
