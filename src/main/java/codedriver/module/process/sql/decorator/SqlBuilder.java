/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.decorator;

import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;

public class SqlBuilder {
    private final StringBuilder sqlSb;

    public SqlBuilder(SqlDecoratorVo sqlDecoratorVo, ProcessSqlTypeEnum fieldTypeEnum) {
        sqlDecoratorVo.init();
        sqlSb = new StringBuilder();
        //提升性能，例如 大于100 则 返回99+ 的场景
        if (ProcessSqlTypeEnum.LIMIT_COUNT.getValue().equals(fieldTypeEnum.getValue())) {
            sqlSb.append(" SELECT COUNT(1) FROM ( ");
        }

        sqlDecoratorVo.setSqlFieldType(fieldTypeEnum.getValue());
        SqlDecoratorChain.firstSqlDecorator.build(sqlSb, sqlDecoratorVo);

        //提升性能，例如 大于100 则 返回99+ 的场景
        if (ProcessSqlTypeEnum.LIMIT_COUNT.getValue().equals(fieldTypeEnum.getValue())) {
            sqlSb.append(" )a ");
        }
    }

    public String build() {
        return sqlSb.toString();
    }

}
