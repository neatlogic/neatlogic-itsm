package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ISqlTable;
import neatlogic.framework.process.workcenter.table.PrioritySqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
        return 5;
    }

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
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, PrioritySqlTable.FieldEnum.UUID.getProName(), PrioritySqlTable.FieldEnum.NAME.getProName());
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(PrioritySqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getGroup(), PrioritySqlTable.FieldEnum.NAME.getProValue());
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(PrioritySqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getSubGroup(), PrioritySqlTable.FieldEnum.NAME.getProValue());
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
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

    @Override
    public Map<String, String> getGroupNameTextMap(List<String> groupNameList) {
        List<PriorityVo> noExistGroupList =  priorityMapper.getPriorityByUuidList(groupNameList);
        if (CollectionUtils.isNotEmpty(noExistGroupList)) {
            return noExistGroupList.stream().collect(Collectors.toMap(PriorityVo::getUuid, PriorityVo::getName));
        }
        return new HashMap<>();
    }
}
