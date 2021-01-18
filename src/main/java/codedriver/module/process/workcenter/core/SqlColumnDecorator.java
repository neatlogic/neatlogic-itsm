package codedriver.module.process.workcenter.core;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;

/**
 * @Title: SqlColumnDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:38
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public class SqlColumnDecorator implements ISqlDecorator {
    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public void build(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
    }

}
