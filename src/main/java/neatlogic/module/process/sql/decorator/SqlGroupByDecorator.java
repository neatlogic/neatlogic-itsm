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
import neatlogic.module.process.sql.IProcessSqlStructure;
import neatlogic.module.process.sql.ProcessSqlStructureFactory;
import org.springframework.stereotype.Component;

@Component
public class SqlGroupByDecorator extends SqlDecoratorBase {
    @Override
    public <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo) {
        IProcessSqlStructure processSqlStructure = ProcessSqlStructureFactory.getProcessSqlStructure("groupBy", sqlDecoratorVo.getSqlFieldType());
        if(processSqlStructure != null) {
            processSqlStructure.doService(sqlSb, sqlDecoratorVo);
        }
    }

    @Override
    public int getSort() {
        return 5;
    }
}
