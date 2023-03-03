/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
