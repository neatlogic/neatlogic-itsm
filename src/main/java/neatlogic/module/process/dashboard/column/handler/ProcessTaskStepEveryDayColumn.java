/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.column.handler;

import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskStepDashboardGroupField;
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
                        new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.ACTIVE_TIME.getValue(), ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue(), true, " STR_TO_DATE(%s.%s,'%%Y-%%m-%%e')")
                )));
            }
        };
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue(), dashboardWidgetChartConfigVo.getGroup(), ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue());
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue(), dashboardWidgetChartConfigVo.getSubGroup(), ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue());
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if(!dataMap.containsKey(ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue())){
                continue;
            }
            groupDataMap.put(dataMap.get(ProcessTaskStepDashboardGroupField.EVERY_DAY.getValue()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
