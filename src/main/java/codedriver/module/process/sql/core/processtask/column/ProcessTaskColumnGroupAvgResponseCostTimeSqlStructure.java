/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.column;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import codedriver.module.process.dashboard.core.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.core.statistics.StatisticsBase;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskColumnGroupAvgResponseCostTimeSqlStructure extends ProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_AVG_RESPONSE_COST_TIME.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "column";
    }

    @Override
    public String getDataSourceHandlerName() {
        return "processtask";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        //补充统计column
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(ProcessTaskDashboardStatistics.AVG_RESPONSE_COST_TIME.getValue());
        List<TableSelectColumnVo> selectColumnVos = avgStatistics.getTableSelectColumn();
        for(TableSelectColumnVo tableSelectColumnVo : selectColumnVos){
            for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                String format = " %s.%s as %s ";
                if (StringUtils.isNotBlank(selectColumnVo.getColumnFormat())) {
                    format = selectColumnVo.getColumnFormat();
                }
                String columnStr = String.format(format, tableSelectColumnVo.getTableShortName(), selectColumnVo.getColumnName(), selectColumnVo.getPropertyName());
                if (!columnList.contains(columnStr)) {
                    columnList.add(columnStr);
                }
            }
        }
        sqlSb.append(String.join(",", columnList));
    }
}
