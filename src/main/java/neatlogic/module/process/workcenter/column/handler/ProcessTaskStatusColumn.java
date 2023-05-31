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
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStatusColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDisplayName() {
        return "common.itsm.processtaskstatus";
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSort() {
        return 5;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        if (processTaskVo.getStatus() != null) {
            return ProcessTaskStatus.getText(processTaskVo.getStatus());
        }
        return StringUtils.EMPTY;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskSqlTable.FieldEnum.STATUS.getProName(), true)
                )));
            }
        };
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONObject statusJson = new JSONObject();
        String status = processTaskVo.getStatus();
        statusJson.put("value", status);
        statusJson.put("text", ProcessTaskStatus.getText(status));
        statusJson.put("color", ProcessTaskStatus.getColor(status));
        return statusJson;
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, ProcessTaskSqlTable.FieldEnum.STATUS.getProName());
        //补充text
        for (int i = 0; i < dbDataMapList.size(); i++) {
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> map = dbDataMapList.get(i);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                if (key.equals(ProcessTaskSqlTable.FieldEnum.STATUS.getProName())) {
                    tmpMap.put("statusText", ProcessTaskStatus.getText(value));
                }
                tmpMap.put(key, value);
            }
            dbDataMapList.set(i, tmpMap);
        }
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskSqlTable.FieldEnum.STATUS.getProName(), dashboardWidgetChartConfigVo.getGroup(), "statusText");
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(ProcessTaskSqlTable.FieldEnum.STATUS.getProName(), dashboardWidgetChartConfigVo.getSubGroup(), "statusText");
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            groupDataMap.put(dataMap.get(ProcessTaskSqlTable.FieldEnum.STATUS.getProName()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }

}
