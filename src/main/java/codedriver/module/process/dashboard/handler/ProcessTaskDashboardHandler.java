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
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
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
	public JSONObject myGetConfig(DashboardWidgetVo widgetVo) {
		DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
		JSONObject processTaskChartConfig = new JSONObject();
		JSONArray processTaskShowChartConfigArray = new JSONArray();
		JSONObject chartConfig = null;
		if (chart != null) {
			chartConfig = chart.getChartConfig();
			if(chartConfig.containsKey("showConfig")) {
				JSONObject showConfigJson = chartConfig.getJSONObject("showConfig");
				if(showConfigJson.containsKey(DashboardShowConfig.TYPE.getValue())) {
					DashboardShowConfigVo aggregateShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.TYPE.getValue());
					processTaskShowChartConfigArray.add(aggregateShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.AGGREGATE.getValue())) {
					DashboardShowConfigVo aggregateShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.AGGREGATE.getValue());
					processTaskShowChartConfigArray.add(aggregateShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
					DashboardShowConfigVo groupShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.GROUPFIELD.getValue());
					JSONArray groupFieldDataArray = groupShowConfig.getDataList();
					groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.PRIORITY.getValue(),ProcessWorkcenterField.PRIORITY.getName())));
					groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.STATUS.getValue(),ProcessWorkcenterField.STATUS.getName())));
					processTaskShowChartConfigArray.add(groupShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
					DashboardShowConfigVo subGroupShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.SUBGROUPFIELD.getValue());
					JSONArray subGroupFieldDataArray = subGroupShowConfig.getDataList();
					subGroupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.PRIORITY.getValue(),ProcessWorkcenterField.PRIORITY.getName())));
					subGroupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s'}", ProcessWorkcenterField.STATUS.getValue(),ProcessWorkcenterField.STATUS.getName())));
					processTaskShowChartConfigArray.add(subGroupShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
					DashboardShowConfigVo maxGroupShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.MAXGROUP.getValue());
					processTaskShowChartConfigArray.add(maxGroupShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.REFRESHTIME.getValue())) {
					DashboardShowConfigVo refreshTimeShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.REFRESHTIME.getValue());
					processTaskShowChartConfigArray.add(refreshTimeShowConfig);
				}
			}
		}
		processTaskChartConfig.put("showConfig", processTaskShowChartConfigArray);
		return processTaskChartConfig;
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
