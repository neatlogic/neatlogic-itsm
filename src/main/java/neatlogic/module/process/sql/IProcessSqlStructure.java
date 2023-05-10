/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
     * 获取 数据源
     * @return 数据源
     */
    String getDataSourceHandlerName();

    /**
     * 补充主体sql
     *
     * @param sqlSb               sql
     * @param sqlDecoratorVo        工单中心参数
     */
    void doService(StringBuilder sqlSb, T sqlDecoratorVo);

}
