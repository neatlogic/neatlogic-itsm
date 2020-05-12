package codedriver.module.process.dashboard.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.QueryResultSet;

import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.service.WorkcenterService;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

	@Autowired
	private WorkcenterService workcenterService;

	@Override
	public String getName() {
		return "processtask";
	}

	@Override
	protected JSONObject myGetData(DashboardWidgetVo widgetVo) {
		DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
		JSONObject jsonObj = new JSONObject();
		if (chart != null) {
			jsonObj = JSONObject.parseObject(widgetVo.getConditionConfig());
			QueryResultSet resultSet = workcenterService.searchTaskIterate(new WorkcenterVo(jsonObj));
			JSONObject preDatas = new JSONObject();
			while(resultSet.hasMoreResults()) {
				JSONArray nextDataList = workcenterService.getSearchIterate(resultSet).getJSONArray("tbodyList");
				if (CollectionUtils.isNotEmpty(nextDataList)) {
					preDatas = chart.getDataMap(nextDataList, widgetVo.getChartConfigObj(),preDatas);
				}
			}
			return chart.getData(preDatas);
		}

		return null;
	}

	@Override
	public JSONObject getChartConfig(DashboardWidgetVo widgetVo) {
		DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
		JSONObject processTaskChartConfig = new JSONObject();
		JSONArray processTaskShowChartConfigArray = new JSONArray();
		JSONObject chartConfig = null;
		if (chart != null) {
			chartConfig = chart.getChartConfig();
			if(chartConfig.containsKey("showConfig")) {
				JSONArray showConfigArray = chartConfig.getJSONArray("showConfig");
				for(Object showConfigObj : showConfigArray) {
					JSONObject showConfigJson = (JSONObject)showConfigObj;
					if(showConfigJson.containsKey(DashboardShowConfig.AGGREGATE.getValue())) {
						JSONObject showAggregateJson = showConfigJson.getJSONObject(DashboardShowConfig.AGGREGATE.getValue());
						processTaskShowChartConfigArray.add(showAggregateJson);
					}
					if(showConfigJson.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
						JSONObject groupFieldJson = showConfigJson.getJSONObject(DashboardShowConfig.GROUPFIELD.getValue());
						JSONArray groupFieldDataArray = groupFieldJson.getJSONArray("dataList");
						groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.PRIORITY.getValue(),ProcessWorkcenterField.PRIORITY.getName())));
						groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.STATUS.getValue(),ProcessWorkcenterField.STATUS.getName())));
						processTaskShowChartConfigArray.add(groupFieldJson);
					}
					if(showConfigJson.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
						JSONObject subGroupFieldJson = showConfigJson.getJSONObject(DashboardShowConfig.SUBGROUPFIELD.getValue());
						JSONArray subGroupFieldDataArray = subGroupFieldJson.getJSONArray("dataList");
						subGroupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.PRIORITY.getValue(),ProcessWorkcenterField.PRIORITY.getName())));
						subGroupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.STATUS.getValue(),ProcessWorkcenterField.STATUS.getName())));
						processTaskShowChartConfigArray.add(subGroupFieldJson);
					}
					if(showConfigJson.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
						JSONObject maxGroupJson = showConfigJson.getJSONObject(DashboardShowConfig.MAXGROUP.getValue());
						processTaskShowChartConfigArray.add(maxGroupJson);
					}
					if(showConfigJson.containsKey(DashboardShowConfig.REFRESHTIME.getValue())) {
						JSONObject refreshTimeJson = showConfigJson.getJSONObject(DashboardShowConfig.REFRESHTIME.getValue());
						processTaskShowChartConfigArray.add(refreshTimeJson);
					}
				}
				
			}
		}
		processTaskChartConfig.put("showConfig", processTaskShowChartConfigArray);
		return null;
	}

	@Override
	public String getType() {
		return "ITSM";
	}

	@Override
	public String getDisplayName() {
		return "ITSM任务数据";
	}

	@Override
	public String getIcon() {
		return "xx-icon";
	}

}
