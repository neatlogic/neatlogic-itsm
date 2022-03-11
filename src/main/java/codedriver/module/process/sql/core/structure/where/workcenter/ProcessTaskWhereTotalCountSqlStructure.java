/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.where.workcenter;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskWhereTotalCountSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.TOTAL_COUNT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" where ");
        buildCommonConditionWhereSql(sqlSb, workcenterVo);
        buildOtherConditionWhereSql(sqlSb, workcenterVo);
        draftCondition(sqlSb, workcenterVo);
    }
}
