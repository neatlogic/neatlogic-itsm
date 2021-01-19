package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;

/**
 * @Title: SqlWhereDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:39
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public class SqlWhereDecorator extends SqlDecoratorBase {
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {

    }

    @Override
    public int getSort() {
        return 4;
    }
}
