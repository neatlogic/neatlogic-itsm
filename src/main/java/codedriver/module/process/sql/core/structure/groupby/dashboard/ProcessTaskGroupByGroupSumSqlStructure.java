/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.groupby.dashboard;

import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dto.DashboardWidgetParamVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.DashboardProcessSqlBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskGroupByGroupSumSqlStructure extends DashboardProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_SUM.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "groupBy";
    }

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        DashboardWidgetChartConfigVo chartVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
        String subGroup = chartVo.getSubGroup();
        StringBuilder subGroupProperty = new StringBuilder(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(subGroup)) {
            IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(subGroup);
            for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                    if (selectColumnVo.getIsPrimary()) {
                        subGroupProperty.append(String.format(", a.%s ", selectColumnVo.getPropertyName()));
                    }
                }
            }
        }
        sqlSb.append(String.format(" group by a.everyday %s", subGroupProperty.toString()));
    }
}
