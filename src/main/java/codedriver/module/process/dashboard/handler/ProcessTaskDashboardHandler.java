package codedriver.module.process.dashboard.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
			//补充分组条件所有属性
			Map<String, String> valueTextMap =  new HashMap<String,String>();
			if(configChart.containsKey("groupfield")) {
				for (ProcessWorkcenterField s : ProcessWorkcenterField.values()) {
					if(s.getValue().equals(configChart.getString("groupfield"))||s.getValue().equals(configChart.getString("subgroupfield"))) {
						JSONArray dataList = ProcessTaskConditionFactory.getHandler(s.getValue()).getConfig().getJSONArray("dataList");
						if(CollectionUtils.isNotEmpty(dataList)) {
							for(Object obj:dataList) {
								JSONObject json = (JSONObject)obj;
								valueTextMap.put(json.getString("value"), json.getString("text"));
							}
						}
					}
				}
			}
			if(valueTextMap.size()>0) {
				preDatas.put("valueTextMap", valueTextMap);
			}
			while(resultSet.hasMoreResults()) {
				JSONArray nextDataList = workcenterService.getSearchIterate(resultSet).getJSONArray("tbodyList");
				if (CollectionUtils.isNotEmpty(nextDataList)) {
					preDatas = chart.getDataMap(nextDataList,configChart,preDatas);
				}
			}
			return chart.getData(preDatas);
		}
		return null;
	}

	private void getGroupFieldDataArray(JSONArray groupFieldDataArray,DashboardWidgetVo widgetVo,List<ProcessWorkcenterField> fieldList,Boolean isSub) {
		JSONObject groupFieldJson = new JSONObject();
		for(ProcessWorkcenterField groupField : fieldList) {
			if(!isSub&&ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())) {
				groupFieldJson = ProcessTaskConditionFactory.getHandler(groupField.getValue()).getConfig();
				groupFieldJson.remove("isMultiple");
				groupFieldJson.put("handler", ProcessTaskConditionFactory.getHandler(groupField.getValue()).getHandler(ProcessConditionModel.CUSTOM.getValue()));
				
			}
			groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(),groupField.getName(),groupFieldJson)));
		}
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
					getGroupFieldDataArray(groupShowConfig.getDataList(),widgetVo,Arrays.asList(
							ProcessWorkcenterField.PRIORITY,
							ProcessWorkcenterField.STATUS,
							ProcessWorkcenterField.CHANNELTYPE,
							ProcessWorkcenterField.CHANNEL
							),false);
					processTaskShowChartConfigArray.add(groupShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
					DashboardShowConfigVo subGroupShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.SUBGROUPFIELD.getValue());
					getGroupFieldDataArray(subGroupShowConfig.getDataList(),widgetVo,Arrays.asList(
							ProcessWorkcenterField.PRIORITY,
							ProcessWorkcenterField.STATUS,
							ProcessWorkcenterField.CHANNELTYPE,
							ProcessWorkcenterField.CHANNEL
							),true);
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
