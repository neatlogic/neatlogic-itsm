/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.core.structure.limit.workcenter;

import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskLimitLimitCountSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.LIMIT_COUNT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "limit";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(String.format(" limit %d,%d ", workcenterVo.getStartNum(), workcenterVo.getExpectOffsetRowNum()));
    }

}
