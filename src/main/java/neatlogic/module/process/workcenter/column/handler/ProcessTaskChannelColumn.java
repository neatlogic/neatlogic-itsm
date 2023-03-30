package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ChannelSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProcessTaskChannelColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Autowired
    ChannelMapper channelMapper;

    @Override
    public String getName() {
        return "channel";
    }

    @Override
    public String getDisplayName() {
        return "handler.processtask.column.channel";
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
        return 9;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo taskVo) {
        return getValue(taskVo).toString();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        if (processTaskVo.getChannelVo() != null) {
            return processTaskVo.getChannelVo().getName();
        }
        return "服务已被删除";
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ChannelSqlTable(), Arrays.asList(
                        new SelectColumnVo(ChannelSqlTable.FieldEnum.UUID.getValue(), ChannelSqlTable.FieldEnum.UUID.getProValue(),true),
                        new SelectColumnVo(ChannelSqlTable.FieldEnum.NAME.getValue(), ChannelSqlTable.FieldEnum.NAME.getProValue())
                )));
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ChannelSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue(), ChannelSqlTable.FieldEnum.UUID.getValue()));
                }}));
            }
        };
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, ChannelSqlTable.FieldEnum.UUID.getProName(), ChannelSqlTable.FieldEnum.NAME.getProName());
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(ChannelSqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getGroup(), ChannelSqlTable.FieldEnum.NAME.getProValue());
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(ChannelSqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getSubGroup(), ChannelSqlTable.FieldEnum.NAME.getProValue());
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if(!dataMap.containsKey(ChannelSqlTable.FieldEnum.UUID.getProValue())){
                continue;
            }
            groupDataMap.put(dataMap.get(ChannelSqlTable.FieldEnum.UUID.getProValue()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }

    @Override
    public Map<String, String> getGroupNameTextMap(List<String> groupNameList) {
        List<ChannelVo> noExistGroupList =  channelMapper.getChannelByUuidList(groupNameList);
        if (CollectionUtils.isNotEmpty(noExistGroupList)) {
            return noExistGroupList.stream().collect(Collectors.toMap(ChannelVo::getUuid, ChannelVo::getName));
        }
        return new HashMap<>();
    }
}
