/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.decorator;

import codedriver.framework.process.dto.SqlDecoratorVo;

public abstract class SqlDecoratorBase implements ISqlDecorator{

    protected ISqlDecorator nextSqlDecorator;

    @Override
    public <T extends SqlDecoratorVo> void build(StringBuilder sqlSb, T sqlDecoratorVo){
        myBuild(sqlSb,sqlDecoratorVo);
        //最后一步 limit 没有 next
        if(nextSqlDecorator != null) {
            //构造下一部分 sql
            nextSqlDecorator.build(sqlSb, sqlDecoratorVo);
        }
    }

    public abstract <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo);

    @Override
    public ISqlDecorator getNextSqlDecorator() {
        return nextSqlDecorator;
    }

    @Override
    public void setNextSqlDecorator(ISqlDecorator nextSqlDecorator) {
        this.nextSqlDecorator = nextSqlDecorator;
    }
}
