package codedriver.module.process.workcenter.core;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTable;

/**
 * @Title: SqlBuilder
 * @Package: codedriver.module.process.workcenter.core
 * @Description: 构建工单查询sql
 * @Author: 89770
 * @Date: 2021/1/15 14:16
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public class SqlBuilder {
    private final StringBuilder sqlSb;
    public SqlBuilder(){
        sqlSb = new StringBuilder();
        sqlSb.append("SELECT ");
    }

    public String build(){
        return sqlSb.toString();
    }

    public SqlBuilder withSelectDistinctIdColumn(){
        ProcessTaskSqlTable processTaskSqlTable = new ProcessTaskSqlTable();
        sqlSb.append(String.format(" DISTINCT %s.%s ",processTaskSqlTable.getShortName(),processTaskSqlTable.getJoinKey()));
        return this;
    }

    public SqlBuilder withSelectCountColumn(){
        ProcessTaskSqlTable processTaskSqlTable = new ProcessTaskSqlTable();
        sqlSb.append(" COUNT(1) ");

        return this;
    }

    public SqlBuilder withSelectColumn(WorkcenterVo workcenterVo){
        new SqlColumnDecorator().build(sqlSb,workcenterVo);
        return this;
    }

    private SqlBuilder buildFromTable(){
return this;
    }

    //public SqlBuilder with


}
