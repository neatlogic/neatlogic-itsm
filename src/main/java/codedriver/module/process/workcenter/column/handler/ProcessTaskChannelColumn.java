package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinOnVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ChannelSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
        return "服务";
    }

    /*@Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        String channelUuid = json.getString(this.getName());
        String channelName = StringUtils.EMPTY;
        ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
        if (channelVo != null) {
            channelName = channelVo.getName();
        }
        return channelName;
    }*/

    /*@Override
    public JSONObject getMyValueText(JSONObject json) {
        String channelUuid = json.getString(this.getName());
        JSONObject channelJson = new JSONObject();
        ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
        if (channelVo != null) {
            channelJson.put("value", channelUuid);
            channelJson.put("text", channelVo.getName());
            channelJson.put("color", channelVo.getColor());
        }
        return channelJson;
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
        return 9;
    }

    /*@Override
    public Object getSimpleValue(Object json) {
        if (json != null) {
            return json.toString();
        }
        return null;
    }*/

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
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> mapList) {
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
}
