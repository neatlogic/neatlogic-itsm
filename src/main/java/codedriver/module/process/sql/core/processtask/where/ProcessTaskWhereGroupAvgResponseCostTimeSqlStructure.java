/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.where;

import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSlaTimeSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskWhereGroupAvgResponseCostTimeSqlStructure extends ProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_AVG_RESPONSE_COST_TIME.getValue();
    }

    @Override
    public String getDataSourceHandlerName() {
        return "processtask";
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        groupWhereService(workcenterVo, sqlSb);
        sqlSb.append(String.format(" AND %s.`%s` = 'response' and %s.%s = '%s'", new ProcessTaskStepSlaTimeSqlTable().getShortName(),ProcessTaskStepSlaTimeSqlTable.FieldEnum.TYPE.getValue(),
                new ProcessTaskStepSqlTable().getShortName(),ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.SUCCEED.getValue()));
    }
}
