package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskStepUserColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
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
		return "步骤处理人";
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

	/*@Override
	public Object getSimpleValue(Object json) {
		return null;
	}*/

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
	public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> mapList) {
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
			if(dataMap.containsKey("stepUserUserUuid")) {
				groupDataMap.put(dataMap.get("stepUserUserUuid").toString(), dataMap.get("count"));
			}
		}
		return groupDataMap;
	}
}
