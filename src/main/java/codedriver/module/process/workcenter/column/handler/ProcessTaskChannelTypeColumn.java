package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetGroupDefineVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ChannelTypeSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProcessTaskChannelTypeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Autowired
	ChannelTypeMapper channelTypeMapper;
	
	@Override
	public String getName() {
		return "channeltype";
	}

	@Override
	public String getDisplayName() {
		return "服务类型";
	}

	@Override
	public String getSimpleValue(ProcessTaskVo processTaskVo) {
		if (processTaskVo.getChannelVo() != null) {
			if(processTaskVo.getChannelVo().getChannelTypeVo() != null){
				ChannelTypeVo channelTypeVo = processTaskVo.getChannelVo().getChannelTypeVo();
				return channelTypeVo.getName();
			}
		}
		return StringUtils.EMPTY;
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
		return 8;
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONObject channelTypeJson = new JSONObject();
		if (processTaskVo.getChannelVo() != null) {
			if(processTaskVo.getChannelVo().getChannelTypeVo() != null){
				ChannelTypeVo channelTypeVo = processTaskVo.getChannelVo().getChannelTypeVo();
				channelTypeJson.put("value",channelTypeVo.getUuid());
				channelTypeJson.put("text",channelTypeVo.getName());
				channelTypeJson.put("color",channelTypeVo.getColor());
			}
		}
		return channelTypeJson;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ChannelTypeSqlTable(), Arrays.asList(
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.UUID.getValue(),ChannelTypeSqlTable.FieldEnum.UUID.getProValue(),true),
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.NAME.getValue(),ChannelTypeSqlTable.FieldEnum.NAME.getProValue()),
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.COLOR.getValue(),ChannelTypeSqlTable.FieldEnum.COLOR.getProValue())
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return SqlTableUtil.getChannelTypeJoinTableSql();
	}

	@Override
	public void getMyDashboardAllGroupDefine(DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, List<Map<String, Object>> dbDataMapList) {
		getNoExistGroup(dashboardWidgetAllGroupDefineVo, dbDataMapList, ChannelTypeSqlTable.FieldEnum.UUID.getProName(), ChannelTypeSqlTable.FieldEnum.NAME.getProName());
		DashboardWidgetChartConfigVo dashboardWidgetChartConfigVo = dashboardWidgetAllGroupDefineVo.getChartConfigVo();
		if (getName().equals(dashboardWidgetChartConfigVo.getGroup())) {
			DashboardWidgetGroupDefineVo dashboardDataGroupVo = new DashboardWidgetGroupDefineVo(ChannelTypeSqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getGroup(), ChannelTypeSqlTable.FieldEnum.NAME.getProValue());
			dashboardWidgetAllGroupDefineVo.setGroupDefineVo(dashboardDataGroupVo);
		}
		//如果存在子分组
		if (getName().equals(dashboardWidgetChartConfigVo.getSubGroup())) {
			DashboardWidgetGroupDefineVo dashboardDataSubGroupVo = new DashboardWidgetGroupDefineVo(ChannelTypeSqlTable.FieldEnum.UUID.getProValue(), dashboardWidgetChartConfigVo.getSubGroup(), ChannelTypeSqlTable.FieldEnum.NAME.getProValue());
			dashboardWidgetAllGroupDefineVo.setSubGroupDefineVo(dashboardDataSubGroupVo);
		}
	}

	@Override
	public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
		LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
		for (Map<String, Object> dataMap : mapList) {
			if(!dataMap.containsKey(ChannelTypeSqlTable.FieldEnum.UUID.getProValue())){
				continue;
			}
			groupDataMap.put(dataMap.get(ChannelTypeSqlTable.FieldEnum.UUID.getProValue()).toString(), dataMap.get("count"));
		}
		return groupDataMap;
	}

	@Override
	public Map<String, String> getGroupNameTextMap(List<String> groupNameList) {
		List<ChannelTypeVo> noExistGroupList = channelTypeMapper.getChannelTypeByUuidList(groupNameList);
		if (CollectionUtils.isNotEmpty(noExistGroupList)) {
			return noExistGroupList.stream().collect(Collectors.toMap(ChannelTypeVo::getUuid, ChannelTypeVo::getName));
		}
		return new HashMap<>();
	}
}
