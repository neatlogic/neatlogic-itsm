/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.decorator;

import neatlogic.framework.process.dto.SqlDecoratorVo;

public interface ISqlDecorator {

    /**
     * @Description: 构建sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:52
     * @Params: []
     * @Returns: java.lang.String
     **/
    <T extends SqlDecoratorVo> void build(StringBuilder sqlSb, T decorator);


    ISqlDecorator getNextSqlDecorator();

    void setNextSqlDecorator(ISqlDecorator nextSqlDecorator);

    int getSort();

}
