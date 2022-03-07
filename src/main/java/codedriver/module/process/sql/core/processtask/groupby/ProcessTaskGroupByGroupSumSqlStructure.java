/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.groupby;

import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProcessTaskGroupByGroupSumSqlStructure extends ProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_SUM.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "groupBy";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        DashboardWidgetChartConfigVo chartVo = workcenterVo.getDashboardWidgetChartConfigVo();
        String subGroup = chartVo.getSubGroup();
        StringBuilder subGroupProperty = new StringBuilder(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(subGroup)) {
            IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(subGroup);
            for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                Optional<SelectColumnVo> optional = tableSelectColumnVo.getColumnList().stream().filter(SelectColumnVo::getIsPrimary).findFirst();
                optional.ifPresent(selectColumnVo -> subGroupProperty.append(String.format(", a.%s ", selectColumnVo.getPropertyName())));
            }
        }
        sqlSb.append(String.format(" group by a.everyday %s", subGroupProperty.toString()));
    }
}
