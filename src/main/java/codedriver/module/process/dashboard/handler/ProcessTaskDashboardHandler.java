package codedriver.module.process.dashboard.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.QueryResultSet;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
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
			JSONObject preDatas = new JSONObject();
			JSONObject configChart = widgetVo.getChartConfigObj();
			String groupField = configChart.getString(DashboardShowConfig.GROUPFIELD.getValue());
			String subGroupField = configChart.getString(DashboardShowConfig.SUBGROUPFIELD.getValue());
			List<String> resultColumnList = new ArrayList<String>();
			resultColumnList.add(groupField);
			if(StringUtils.isNotBlank(subGroupField)) {
				resultColumnList.add(subGroupField);
			}
			jsonObj.put("resultColumnList", resultColumnList);
			//补充分组条件所有属性
			Map<String, String> valueTextMap =  new HashMap<>();
			if(configChart.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
				for (ProcessWorkcenterField s : ProcessWorkcenterField.values()) {
					if(s.getValue().equals(configChart.getString(DashboardShowConfig.GROUPFIELD.getValue()))||s.getValue().equals(configChart.getString(DashboardShowConfig.SUBGROUPFIELD.getValue()))) {
						JSONArray dataList = ConditionHandlerFactory.getHandler(s.getValue()).getConfig().getJSONArray("dataList");
						if(CollectionUtils.isNotEmpty(dataList)) {
							for(int i = 0; i < dataList.size(); i++) {
								JSONObject json = dataList.getJSONObject(i);
								valueTextMap.put(json.getString("value"), json.getString("text"));
							}
						}
					}
				}
			}
			if(valueTextMap.size()>0) {
				preDatas.put("valueTextMap", valueTextMap);
			}
			WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
			QueryResultSet resultSet = workcenterService.searchTaskIterate(workcenterVo);
			while(resultSet.hasMoreResults()) {
				JSONArray nextDataList = workcenterService.getSearchIterate(resultSet,workcenterVo).getJSONArray("tbodyList");
				if (CollectionUtils.isNotEmpty(nextDataList)) {
					preDatas = chart.getDataMap(nextDataList,configChart,preDatas);
				}
			}
			preDatas.put("configObj", configChart);
			if(configChart.containsKey(DashboardShowConfig.GROUPFIELD.getValue())){
				configChart.put("groupfieldtext", ProcessWorkcenterField.getName(groupField));
			}
			if(configChart.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())){
				configChart.put("subgroupfieldtext", ProcessWorkcenterField.getName(subGroupField));
			}
			//排序、限制数量
			JSONObject data = chart.getData(preDatas);
			if(!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())&&!ChartType.TABLECHART.getValue().equals(widgetVo.getChartType())&&configChart.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
				Integer maxGroup = configChart.getInteger(DashboardShowConfig.MAXGROUP.getValue());
				Set<String> maxGroupSet = new HashSet<String>();
				JSONArray dataList = data.getJSONArray("dataList");
				if(CollectionUtils.isNotEmpty(dataList)) {
					dataList.sort(Comparator.comparing(obj-> ((JSONObject) obj).getInteger("total")).reversed());
					Iterator<Object> dataIterator = dataList.iterator();
					while(dataIterator.hasNext()) {
						JSONObject dataJson = (JSONObject)dataIterator.next();
						if(maxGroupSet.size()>=maxGroup&&!maxGroupSet.contains(dataJson.getString("column"))) {
							dataIterator.remove();
						}else {
							maxGroupSet.add(dataJson.getString("column"));
						}
					}
				}
			}
			return data;
		}
		return null;
	}

	private void getGroupFieldDataArray(JSONArray groupFieldDataArray,DashboardWidgetVo widgetVo,List<ProcessWorkcenterField> fieldList,Boolean isSub) {
		JSONObject groupFieldJson = new JSONObject();
		for(ProcessWorkcenterField groupField : fieldList) {
			if(!isSub&&ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())) {
				groupFieldJson = ConditionHandlerFactory.getHandler(groupField.getValue()).getConfig();
				groupFieldJson.remove("isMultiple");
				groupFieldJson.put("handler", ConditionHandlerFactory.getHandler(groupField.getValue()).getHandler(ProcessConditionModel.CUSTOM.getValue()));
				
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
							ProcessWorkcenterField.CHANNEL,
							ProcessWorkcenterField.STEP_USER,
							ProcessWorkcenterField.OWNER
							),false);
					processTaskShowChartConfigArray.add(groupShowConfig);
				}
				if(showConfigJson.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
					DashboardShowConfigVo subGroupShowConfig = (DashboardShowConfigVo)showConfigJson.get(DashboardShowConfig.SUBGROUPFIELD.getValue());
					getGroupFieldDataArray(subGroupShowConfig.getDataList(),widgetVo,Arrays.asList(
							ProcessWorkcenterField.PRIORITY,
							ProcessWorkcenterField.STATUS,
							ProcessWorkcenterField.CHANNELTYPE,
							ProcessWorkcenterField.CHANNEL,
							ProcessWorkcenterField.STEP_USER,
							ProcessWorkcenterField.OWNER
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
