package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.*;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.UserTable;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProcessTaskOwnerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    UserMapper userMapper;

    @Override
    public String getName() {
        return "owner";
    }

    @Override
    public String getDisplayName() {
        return "上报人";
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
        return 3;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo taskVo) {
        if (taskVo.getOwnerVo() != null) {
            return taskVo.getOwnerVo().getName();
        }
        return null;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        return processTaskVo.getOwnerVo();
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new UserTable(), "owner", Arrays.asList(
                        new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(), "ownerUuid", true),
                        new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(), "ownerName"),
                        new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(), "ownerInfo"),
                        new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(), "ownerVipLevel"),
                        new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(), "ownerPinYin")
                )));
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new UserTable(), "owner", new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.OWNER.getValue(), UserTable.FieldEnum.UUID.getValue()));
                }}));
            }
        };
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, "ownerUuid", "ownerName");
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo("ownerUuid", dashboardWidgetChartConfigVo.getGroup(), "ownerName");
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo("ownerUuid", dashboardWidgetChartConfigVo.getSubGroup(), "ownerName");
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if (dataMap.get("ownerUuid") != null) {
                groupDataMap.put(dataMap.get("ownerUuid").toString(), dataMap.get("count"));
            }
        }
        return groupDataMap;
    }

    @Override
    public Map<String, String> getGroupNameTextMap(List<String> groupNameList) {
        List<UserVo> noExistGroupList = userMapper.getUserByUserUuidList(groupNameList.stream().map(GroupSearch::removePrefix).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(noExistGroupList)) {
            return noExistGroupList.stream().collect(Collectors.toMap(e -> GroupSearch.USER.getValuePlugin() + e.getUuid(), UserVo::getName));
        }
        return new HashMap<>();
    }
}
