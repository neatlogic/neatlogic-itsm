/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
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
