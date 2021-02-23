package codedriver.module.process.workcenter.core;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.workcenter.core.sqldecorator.SqlDecoratorChain;
import codedriver.module.process.workcenter.core.sqldecorator.SqlLimitDecorator;
import codedriver.module.process.workcenter.core.sqldecorator.SqlOrderDecorator;

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

    public SqlBuilder(WorkcenterVo workcenterVo, FieldTypeEnum fieldTypeEnum) {
        sqlSb = new StringBuilder();
        //提升性能，例如 大于100 则 返回99+ 的场景
        if(FieldTypeEnum.LIMIT_COUNT.getValue().equals(fieldTypeEnum.getValue())){
            sqlSb.append(" SELECT COUNT(1) FROM ( ");
        }

        workcenterVo.setSqlFieldType(fieldTypeEnum.getValue());
        SqlDecoratorChain.firstSqlDecorator.build(sqlSb, workcenterVo);

        //提升性能，例如 大于100 则 返回99+ 的场景
        if(FieldTypeEnum.LIMIT_COUNT.getValue().equals(fieldTypeEnum.getValue())){
            sqlSb.append(" )a ");
        }
    }

    public String build() {
        return sqlSb.toString();
    }

    /**
     * @Description: 构建 order 排序
     * @Author: 89770
     * @Date: 2021/1/19 15:14
     * @Params: [workcenterVo]
     * @Returns: void
     **/
    public void buildOrder(WorkcenterVo workcenterVo) {
        new SqlOrderDecorator().build(sqlSb, workcenterVo);
    }

    /**
     * @Description: 构建 limit 分页
     * @Author: 89770
     * @Date: 2021/1/19 15:16
     * @Params: [workcenterVo]
     * @Returns: void
     **/
    public void buildLimit(WorkcenterVo workcenterVo) {
        new SqlLimitDecorator().build(sqlSb, workcenterVo);
    }

}
