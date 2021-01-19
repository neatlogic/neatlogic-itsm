package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.workcenter.core.SqlBuilder;
import codedriver.module.process.workcenter.core.table.ISqlTable;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTable;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTableFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
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
        buildFieldMap.put(SqlBuilder.FieldTypeEnum.DISTINCT_ID.getValue(), (workcenterVo, sqlSb) -> {
            ProcessTaskSqlTable processTaskSqlTable = new ProcessTaskSqlTable();
            sqlSb.append(String.format(" DISTINCT %s.%s ", processTaskSqlTable.getShortName(), processTaskSqlTable.getJoinKey()));
        });

        buildFieldMap.put(SqlBuilder.FieldTypeEnum.COUNT.getValue(), (workcenterVo, sqlSb) -> {
            sqlSb.append(" COUNT(1) ");
        });

        buildFieldMap.put(SqlBuilder.FieldTypeEnum.FIELD.getValue(), (workcenterVo, sqlSb) -> {
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
        Map<String, ISqlTable> tableComponentMap =  ProcessTaskSqlTableFactory.tableComponentMap;
        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            IProcessTaskColumn column = entry.getValue();
            ISqlTable sqlTable = tableComponentMap.get(column.getSqlTableName());
            if(sqlTable != null){
                sqlSb.append(String.format("%s.%s,",sqlTable.getShortName(),column.getName()));
            }
        }
    }

    @Override
    public int getSort() {
        return 2;
    }

}
