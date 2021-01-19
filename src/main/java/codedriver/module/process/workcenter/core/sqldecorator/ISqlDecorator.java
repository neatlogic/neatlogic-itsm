package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;

/**
 * @Title: SqlBuidlerDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: 构建sql装饰接口
 * @Author: 89770
 * @Date: 2021/1/15 11:33
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public interface ISqlDecorator {

    /**
     * @Description: 构建sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:52
     * @Params: []
     * @Returns: java.lang.String
     **/
    void build(StringBuilder sqlSb, WorkcenterVo workcenterVo);


    public ISqlDecorator getNextSqlDecorator();

    public void setNextSqlDecorator(ISqlDecorator nextSqlDecorator);

    public int getSort();

}
