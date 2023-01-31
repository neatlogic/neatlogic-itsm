/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
