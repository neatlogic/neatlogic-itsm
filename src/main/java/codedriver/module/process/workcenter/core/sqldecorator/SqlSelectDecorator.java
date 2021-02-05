package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import org.springframework.stereotype.Component;

/**
 * @Title: SqlColumnDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:38
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlSelectDecorator extends SqlDecoratorBase {
    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append("SELECT ");
    }

    @Override
    public int getSort() {
        return 1;
    }

}
