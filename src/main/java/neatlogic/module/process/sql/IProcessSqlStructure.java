/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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
