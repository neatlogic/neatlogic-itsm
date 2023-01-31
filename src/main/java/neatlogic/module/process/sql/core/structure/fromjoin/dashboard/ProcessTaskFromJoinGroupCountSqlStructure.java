/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.core.structure.fromjoin.dashboard;

import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.DashboardProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskFromJoinGroupCountSqlStructure extends DashboardProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_COUNT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "fromJoin";
    }

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        getJoinTableOfGroupColumn(sqlSb, dashboardWidgetParamVo);
    }
}
