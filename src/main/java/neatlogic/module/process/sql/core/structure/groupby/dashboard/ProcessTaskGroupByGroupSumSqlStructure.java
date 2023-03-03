/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.sql.core.structure.groupby.dashboard;

import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.DashboardProcessSqlBase;
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
        String everyday = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup();
        sqlSb.append(String.format("group by a.%s %s", everyday,subGroupProperty));
    }
}
