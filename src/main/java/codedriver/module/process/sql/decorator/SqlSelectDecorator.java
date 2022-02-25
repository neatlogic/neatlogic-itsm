/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.decorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import org.springframework.stereotype.Component;

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
