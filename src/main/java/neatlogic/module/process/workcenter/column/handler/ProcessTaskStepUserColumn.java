package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.util.SqlTableUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProcessTaskStepUserColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    TeamMapper teamMapper;

    @Override
    public String getName() {
        return "stepuser";
    }

    @Override
    public String getDisplayName() {
        return "common.stepuser";
    }

    @Override
    public Boolean getDisabled() {
        return true;
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
    public Boolean getMyIsShow() {
        return false;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        return null;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return SqlTableUtil.getTableSelectColumn();
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return SqlTableUtil.getStepUserJoinTableSql();
    }

    @Override
    public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
        getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, "stepUserUserUuid", "stepUserUserName");
        DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
        if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo("stepUserUserUuid", dashboardWidgetChartConfigVo.getGroup(), "stepUserUserName");
            dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
        }
        //如果存在子分组
        if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
            DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo("stepUserUserUuid", dashboardWidgetChartConfigVo.getSubGroup(), "stepUserUserName");
            dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
        LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : mapList) {
            if (dataMap.containsKey("stepUserUserUuid")) {
                groupDataMap.put(dataMap.get("stepUserUserUuid").toString(), dataMap.get("count"));
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
