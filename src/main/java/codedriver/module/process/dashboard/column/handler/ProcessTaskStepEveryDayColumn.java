/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.column.handler;

import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.module.process.dashboard.constvalue.ProcessTaskStepDashboardGroupField;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStepEveryDayColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue();
    }

    @Override
    public String getDisplayName() {
        return "每天";
    }

    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.CUSTOM.getValue();
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Integer getSort() {
        return 2;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        return processTaskVo.getId();
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskStepSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.ACTIVE_TIME.getValue(), "everyday", true, " STR_TO_DATE(%s.%s,'%%Y-%%m-%%e')")
                )));
            }
        };
    }

    @Override
    public void getMyDashboardDataVo(DashboardWidgetDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
        if (getName().equals(workcenterVo.getDashboardWidgetChartConfigVo().getGroup())) {
            DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo("everyday", workcenterVo.getDashboardWidgetChartConfigVo().getGroup(), "everyday", workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap());
            dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup())) {
            DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
            dashboardDataSubGroupVo = new DashboardDataSubGroupVo("everyday", workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup(), "everyday");
            dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if(!dataMap.containsKey("everyday")){
                continue;
            }
            groupDataMap.put(dataMap.get("everyday").toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
