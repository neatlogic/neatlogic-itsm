package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTableFactory;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
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

            sqlSb.append(String.format(" DISTINCT %s.%s ", processTaskSqlTable.getShortName(), "id"));
            //拼接排序字段
            JSONArray sortList = workcenterVo.getSortList();
            if (CollectionUtils.isNotEmpty(sortList)) {
                for (Object sortObj : sortList) {
                    JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                    for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                        String key = entry.getKey();
                        IProcessTaskColumn processTaskColumn = ProcessTaskColumnFactory.getHandler(key);
                        if (processTaskColumn != null && processTaskColumn.getIsSort()) {
                            sqlSb.append(String.format(",%s.%s", processTaskColumn.getSortSqlTable().getShortName(), processTaskColumn.getSortSqlColumn()));
                        }
                    }
                }
            } else {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue());
                sqlSb.append(String.format(",%s.%s", new ProcessTaskSqlTable().getShortName(), column.getSortSqlColumn()));
            }
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
        Map<String, ISqlTable> tableComponentMap = ProcessTaskSqlTableFactory.tableComponentMap;
        List<String> columnList = new ArrayList<>();
        for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
            //去掉沒有勾选的thead
            if (theadVo.getIsShow() != 1) {
                continue;
            }
            if (columnComponentMap.containsKey(theadVo.getName())) {
                IProcessTaskColumn column = columnComponentMap.get(theadVo.getName());
                for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                    for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                        String columnStr = String.format(" %s.%s as %s ", tableSelectColumnVo.getTableShortName(), selectColumnVo.getColumnName(), selectColumnVo.getPropertyName());
                        if (!columnList.contains(columnStr)) {
                            columnList.add(columnStr);
                        }
                    }
                }

            }
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
