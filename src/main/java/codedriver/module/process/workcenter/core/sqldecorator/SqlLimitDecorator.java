package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @Title: SqlLimitDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:41
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlLimitDecorator extends SqlDecoratorBase {
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        if(FieldTypeEnum.DISTINCT_ID.getValue().equals(workcenterVo.getSqlFieldType())) {
            sqlSb.append(String.format(" limit %d,%d ", workcenterVo.getStartNum(), workcenterVo.getPageSize()));
        }
    }

    @Override
    public int getSort() {
        return 6;
    }
}
