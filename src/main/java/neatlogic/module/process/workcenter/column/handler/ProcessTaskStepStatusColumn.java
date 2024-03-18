/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStepStatusColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Override
    public String getName() {
        return "stepstatus";
    }

    @Override
    public String getDisplayName() {
        return "步骤状态";
    }

    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Integer getSort() {
        return 5;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        return null;
    }

    @Override
    public Boolean getMyIsShow() {
        return false;
    }

    @Override
    public Boolean getDisabled() {
        return true;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskStepSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName(), true)
                )));
            }
        };
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName());
        //补充text
        for (int i = 0; i < dbDataMapList.size(); i++) {
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> map = dbDataMapList.get(i);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                if (key.equals(ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName())) {
                    tmpMap.put("statusText", ProcessTaskStatus.getText(value));
                }
                tmpMap.put(key, value);
            }
            dbDataMapList.set(i, tmpMap);
        }
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName(), dashboardWidgetChartConfigVo.getGroup(), "statusText");
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName(), dashboardWidgetChartConfigVo.getSubGroup(), "statusText");
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if (dataMap.containsKey(ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName())) {
                groupDataMap.put(dataMap.get(ProcessTaskStepSqlTable.FieldEnum.STATUS.getProName()).toString(), dataMap.get("count"));
            }
        }
        return groupDataMap;
    }
}
