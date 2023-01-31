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
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskDashboardGroupField;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskEveryDayColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return ProcessTaskDashboardGroupField.EVERY_DAY.getValue();
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
                add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.START_TIME.getValue(), "everyday", true, " STR_TO_DATE(%s.%s,'%%Y-%%m-%%e')")
                )));
            }
        };
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> mapList) {
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo("everyday", dashboardWidgetChartConfigVo.getGroup(), "everyday");
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo("everyday", dashboardWidgetChartConfigVo.getSubGroup(), "everyday");
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
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
