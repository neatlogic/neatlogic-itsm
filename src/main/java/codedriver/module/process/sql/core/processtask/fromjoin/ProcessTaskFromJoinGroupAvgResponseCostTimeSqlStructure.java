/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.fromjoin;

import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import codedriver.module.process.dashboard.core.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.core.statistics.StatisticsBase;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ProcessTaskFromJoinGroupAvgResponseCostTimeSqlStructure extends ProcessSqlBase {

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
        return "fromJoin";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        List<JoinTableColumnVo>  joinTableColumnList = getJoinTableOfGroupColumn(sqlSb, workcenterVo);
        //补充统计joinTable
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(ProcessTaskDashboardStatistics.AVG_RESPONSE_COST_TIME.getValue());
        for(JoinTableColumnVo joinTableColumnVo : avgStatistics.getJoinTableColumnList()) {
            if (joinTableColumnList.stream().noneMatch(o-> Objects.equals(o.getHash(),joinTableColumnVo.getHash()))) {
                sqlSb.append(joinTableColumnVo.toSqlString());
            }
        }
    }
}
