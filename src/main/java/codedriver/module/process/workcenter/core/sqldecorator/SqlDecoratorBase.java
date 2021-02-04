package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;

/**
 * @Title: SqlDecoratorBase
 * @Package: codedriver.module.process.workcenter.core.sqldecorator
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/19 15:36
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public abstract class SqlDecoratorBase implements ISqlDecorator{

    protected ISqlDecorator nextSqlDecorator;

    @Override
    public void build(StringBuilder sqlSb, WorkcenterVo workcenterVo){
        myBuild(sqlSb,workcenterVo);
        //最后一步 limit 没有 next
        if(nextSqlDecorator != null) {
            //构造下一部分 sql
            nextSqlDecorator.build(sqlSb, workcenterVo);
        }
    }

    public abstract void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo);

    @Override
    public ISqlDecorator getNextSqlDecorator() {
        return nextSqlDecorator;
    }

    @Override
    public void setNextSqlDecorator(ISqlDecorator nextSqlDecorator) {
        this.nextSqlDecorator = nextSqlDecorator;
    }
}
