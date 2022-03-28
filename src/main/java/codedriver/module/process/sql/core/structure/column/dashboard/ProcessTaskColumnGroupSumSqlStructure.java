/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.column.dashboard;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dto.DashboardWidgetParamVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.DashboardProcessSqlBase;
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
