package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.*;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.PrioritySqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskPriorityColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    PriorityMapper priorityMapper;

    @Override
    public String getName() {
        return "priority";
    }

    @Override
    public String getDisplayName() {
        return "优先级";
    }

   /* @Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        String priorityUuid = json.getString(this.getName());
        JSONObject priorityJson = new JSONObject();
        if (StringUtils.isNotBlank(priorityUuid)) {
            priorityJson.put("value", priorityUuid);
            PriorityVo priority = priorityMapper.getPriorityByUuid(priorityUuid);
            if (priority != null) {
                priorityJson.put("text", priority.getName());
                priorityJson.put("color", priority.getColor());
            }
        }
        return priorityJson;
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
        // TODO Auto-generated method stub
        return 3;
    }

   /* @Override
    public Object getSimpleValue(Object json) {
        String priority = null;
        if (json != null) {
            priority = JSONObject.parseObject(json.toString()).getString("text");
        }
        return priority;
    }*/

    @Override
    public String getSimpleValue(ProcessTaskVo taskVo) {
        if(taskVo.getPriority() != null){
            return taskVo.getPriority().getName();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONObject priorityJson = new JSONObject();
        if (processTaskVo.getPriority() != null) {
            priorityJson.put("value", processTaskVo.getPriority().getUuid());
            priorityJson.put("text", processTaskVo.getPriority().getName());
            priorityJson.put("color", processTaskVo.getPriority().getColor());
        }
        return priorityJson;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new PrioritySqlTable(), Arrays.asList(
                        new SelectColumnVo(PrioritySqlTable.FieldEnum.UUID.getValue(), PrioritySqlTable.FieldEnum.UUID.getProValue(), true),
                        new SelectColumnVo(PrioritySqlTable.FieldEnum.NAME.getValue(), PrioritySqlTable.FieldEnum.NAME.getProValue()),
                        new SelectColumnVo(PrioritySqlTable.FieldEnum.COLOR.getValue(), PrioritySqlTable.FieldEnum.COLOR.getProValue())
                )));
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new PrioritySqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.PRIORITY_UUID.getValue(), PrioritySqlTable.FieldEnum.UUID.getValue()));
                }}));
            }
        };
    }

    @Override
    public String getMySortSqlColumn(Boolean isColumn) {
        return PrioritySqlTable.FieldEnum.SORT.getValue();
    }

    @Override
    public ISqlTable getMySortSqlTable() {
        return new PrioritySqlTable();
    }

    @Override
    public void getMyDashboardDataVo(DashboardDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
        if (getName().equals(workcenterVo.getDashboardConfigVo().getGroup())) {
            DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo(PrioritySqlTable.FieldEnum.UUID.getProValue(), workcenterVo.getDashboardConfigVo().getGroup(), PrioritySqlTable.FieldEnum.NAME.getProValue(), workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
            dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(workcenterVo.getDashboardConfigVo().getSubGroup())) {
            DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
            dashboardDataSubGroupVo = new DashboardDataSubGroupVo(PrioritySqlTable.FieldEnum.UUID.getProValue(), workcenterVo.getDashboardConfigVo().getSubGroup(), PrioritySqlTable.FieldEnum.NAME.getProValue());
            dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if(!dataMap.containsKey(PrioritySqlTable.FieldEnum.UUID.getProValue())){
                continue;
            }
            groupDataMap.put(dataMap.get(PrioritySqlTable.FieldEnum.UUID.getProValue()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
