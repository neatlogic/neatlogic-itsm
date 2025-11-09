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

package neatlogic.module.process.sql;

import neatlogic.framework.process.dto.SqlDecoratorVo;

public interface IProcessSqlStructure<T extends SqlDecoratorVo> {
    /**
     * 获取 类型名
     * @return 类型名
     */
    String getName();

    /**
     * 获取 sql部分名 select｜column｜fromJoin｜where｜group|limit|order
     * @return sql部分名
     */
    String getSqlStructureName();

    /**
     * 补充主体sql
     *
     * @param sqlSb               sql
     * @param sqlDecoratorVo        工单中心参数
     */
    void doService(StringBuilder sqlSb, T sqlDecoratorVo);

}
