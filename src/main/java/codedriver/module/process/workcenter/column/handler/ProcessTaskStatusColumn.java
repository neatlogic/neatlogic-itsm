package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
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
        return "工单状态";
    }

  /*  @Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        JSONObject statusJson = new JSONObject();
        String status = json.getString(this.getName());
        statusJson.put("value", status);
        statusJson.put("text", ProcessTaskStatus.getText(status));
        statusJson.put("color", ProcessTaskStatus.getColor(status));
        return statusJson;
    }

    @Override
    public JSONObject getMyValueText(JSONObject json) {
        return (JSONObject) getMyValue(json);
    }*/

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

   /* @Override
    public Object getSimpleValue(Object json) {
        String status = null;
        if (json != null) {
            status = JSONObject.parseObject(json.toString()).getString("text");
        }
        return status;
    }*/

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
                        new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskSqlTable.FieldEnum.STATUS.getProValue(), true)
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
    public void getMyDashboardDataVo(DashboardDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
        //补充text
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> map = mapList.get(i);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals(ProcessTaskSqlTable.FieldEnum.STATUS.getProValue())) {
                    tmpMap.put("statusText", ProcessTaskStatus.getText(value.toString()));
                }
                tmpMap.put(key, value);
            }
            mapList.set(i, tmpMap);
        }
        //
        if (getName().equals(workcenterVo.getDashboardConfigVo().getGroup())) {
            DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo(ProcessTaskSqlTable.FieldEnum.STATUS.getProValue(), workcenterVo.getDashboardConfigVo().getGroup(), "statusText", workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
            dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(workcenterVo.getDashboardConfigVo().getSubGroup())) {
            DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
            dashboardDataSubGroupVo = new DashboardDataSubGroupVo(ProcessTaskSqlTable.FieldEnum.STATUS.getProValue(), workcenterVo.getDashboardConfigVo().getSubGroup(), "statusText");
            dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            groupDataMap.put(dataMap.get(ProcessTaskSqlTable.FieldEnum.STATUS.getProValue()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
