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

package neatlogic.module.process.sql.core.structure.column.dashboard;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.DashboardProcessSqlBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskColumnGroupSumSqlStructure extends DashboardProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_SUM.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "column";
    }

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        List<String> groupColumns = Arrays.asList(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getSubGroup(),dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup());
        for(String groupColumn : groupColumns) {
            if (columnComponentMap.containsKey(groupColumn)) {
                IProcessTaskColumn column = columnComponentMap.get(groupColumn);
                for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                    if (tableSelectColumnVo.getColumnList().stream().anyMatch(SelectColumnVo::getIsPrimary)) {
                        for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                            String format = " a.%s ";
                            String columnStr = String.format(format, selectColumnVo.getPropertyName());
                            if (!columnList.contains(columnStr)) {
                                columnList.add(columnStr);
                            }
                        }
                    }
                }

            }
        }
        sqlSb.append(String.join(",", columnList));
        sqlSb.append(", SUM( b.COUNT  ) AS `count`  ");
    }

}
