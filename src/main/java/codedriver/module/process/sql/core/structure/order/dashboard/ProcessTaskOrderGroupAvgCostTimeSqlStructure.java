/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.order.dashboard;

import codedriver.framework.process.dto.DashboardWidgetParamVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.DashboardProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskOrderGroupAvgCostTimeSqlStructure extends DashboardProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_AVG_COST_TIME.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "order";
    }

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        sqlSb.append(" order by count DESC");
    }
}
