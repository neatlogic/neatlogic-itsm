package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.*;
import codedriver.framework.process.workcenter.table.CatalogSqlTable;
import codedriver.framework.process.workcenter.table.ChannelSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskCatalogColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    CatalogMapper catalogMapper;

    @Override
    public String getName() {
        return "catalog";
    }

    @Override
    public String getDisplayName() {
        return "服务目录";
    }

    /*@Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        String catalogUuid = json.getString(this.getName());
        String catalogName = StringUtils.EMPTY;
        CatalogVo catalogVo = catalogMapper.getCatalogByUuid(catalogUuid);
        if (catalogVo != null) {
            catalogName = catalogVo.getName();
        }
        return catalogName;
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
        return 7;
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
            if (processTaskVo.getChannelVo().getParent() != null) {
                return processTaskVo.getChannelVo().getParent().getName();
            } else {
                return "服务目录已被删除";
            }
        }
        return "服务已被删除";
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new CatalogSqlTable(), Arrays.asList(new SelectColumnVo(CatalogSqlTable.FieldEnum.UUID.getValue(), CatalogSqlTable.FieldEnum.UUID.getProValue(),true)
                        , new SelectColumnVo(CatalogSqlTable.FieldEnum.NAME.getValue(), CatalogSqlTable.FieldEnum.NAME.getProValue()))));
                ;
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
                add(new JoinTableColumnVo(new ChannelSqlTable(), new CatalogSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ChannelSqlTable.FieldEnum.PARENT_UUID.getValue(), CatalogSqlTable.FieldEnum.UUID.getValue()));
                }}));
            }
        };
    }

    @Override
    public void getMyDashboardDataVo(DashboardDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
        if (getName().equals(workcenterVo.getDashboardConfigVo().getGroup())) {
            DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo(CatalogSqlTable.FieldEnum.UUID.getProValue(), workcenterVo.getDashboardConfigVo().getGroup(), CatalogSqlTable.FieldEnum.NAME.getProValue(), workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
            dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(workcenterVo.getDashboardConfigVo().getSubGroup())) {
            DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
            dashboardDataSubGroupVo = new DashboardDataSubGroupVo(CatalogSqlTable.FieldEnum.UUID.getProValue(), workcenterVo.getDashboardConfigVo().getSubGroup(), CatalogSqlTable.FieldEnum.NAME.getProValue());
            dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            groupDataMap.put(dataMap.get(CatalogSqlTable.FieldEnum.UUID.getProValue()).toString(), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
