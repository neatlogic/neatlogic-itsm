/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.sql.decorator;

import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;

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
