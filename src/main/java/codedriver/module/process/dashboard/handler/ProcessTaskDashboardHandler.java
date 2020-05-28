package codedriver.module.process.dashboard.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.QueryResultSet;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
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
			JSONObject configChart = widgetVo.getChartConfigObj();
			while(resultSet.hasMoreResults()) {
				JSONArray nextDataList = workcenterService.getSearchIterate(resultSet).getJSONArray("tbodyList");
				if (CollectionUtils.isNotEmpty(nextDataList)) {
					preDatas = chart.getDataMap(nextDataList,configChart,preDatas);
				}
			}
			//补充分组条件所有属性
			Map<String, String> valueTextMap =  new HashMap<String,String>();
			if(configChart.containsKey("groupfield")) {
				for (ProcessWorkcenterField s : ProcessWorkcenterField.values()) {
					if(s.getValue().equals(configChart.getString("groupfield"))||s.getValue().equals(configChart.getString("subgroupfield"))) {
						JSONArray dataList = ProcessTaskConditionFactory.getHandler(s.getValue()).getConfig().getJSONArray("dataList");
						for(Object obj:dataList) {
							JSONObject json = (JSONObject)obj;
							valueTextMap.put(json.getString("value"), json.getString("text"));
						}
					}
				}
			}
			preDatas.put("valueTextMap", valueTextMap);
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
					JSONObject priorityJson = new JSONObject();
					JSONObject statusJson = new JSONObject();
					if(ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())) {
						priorityJson = ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.PRIORITY.getValue()).getConfig();
						priorityJson.remove("isMultiple");
						priorityJson.put("handler", ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.PRIORITY.getValue()).getHandler(ProcessConditionModel.CUSTOM.getValue()));
						statusJson = ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.STATUS.getValue()).getConfig();
						statusJson.remove("isMultiple");
						statusJson.put("handler", ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.STATUS.getValue()).getHandler(ProcessConditionModel.CUSTOM.getValue()));
					}
					groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", ProcessWorkcenterField.PRIORITY.getValue(),ProcessWorkcenterField.PRIORITY.getName(),priorityJson)));
					groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", ProcessWorkcenterField.STATUS.getValue(),ProcessWorkcenterField.STATUS.getName(),statusJson)));
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
				if(showConfigJson.containsKey(DashboardShowConfig.COLOR.getValue())) {
					DashboardShowConfigVo refreshTimeShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.COLOR.getValue());
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
