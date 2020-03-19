package codedriver.module.process.dashboard.handler;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.ChartDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

	@Override
	public String getName() {
		return "ITSM任务数据";
	}

	/*
	 * 
	 * { "groupField":"urgency", "aggregate":"count",
	 * "subGroupField":"","valueField":"", "displaycolumn":["owner","title"],
	 * "sort":"createTime", "limit":10 }
	 */

//	protected ChartDataVo myGetData2(DashboardWidgetVo widgetVo) {
//		JSONObject resultObj = WorkcenterHandler.doSearch(new WorkcenterVo());
//
//		JSONObject chartConfigObj = widgetVo.getChartConfigObj();
//		if (chartConfigObj != null) {
//			final String groupField = chartConfigObj.getString("groupField");
//			final String subGroupField = chartConfigObj.getString("subGroupField");
//			final String aggregate = chartConfigObj.getString("aggregate");
//			final String valueField = chartConfigObj.getString("valueField");
//
//			ChartDataVo chartDataVo = new ChartDataVo();
//			chartDataVo.setGroupField(groupField);
//			chartDataVo.setSubGroupField(subGroupField);
//			chartDataVo.setValueField("value");
//
//			Map<String, Long> resultMap = null;
//			if (aggregate.equals("sum")) {
//				resultMap = resultObj.getJSONArray("tbodyList").stream().collect(Collectors.groupingBy(map -> ((JSONObject) map).getString(groupField) + (StringUtils.isNotBlank(subGroupField) ? "#" + ((JSONObject) map).getString(subGroupField) : ""), Collectors.summingLong(map -> ((JSONObject) map).getLong(valueField))));
//			} else if (aggregate.equals("count")) {
//				resultMap = resultObj.getJSONArray("tbodyList").stream().collect(Collectors.groupingBy(map -> ((JSONObject) map).getString(groupField) + (StringUtils.isNotBlank(subGroupField) ? "#" + ((JSONObject) map).getString(subGroupField) : ""), Collectors.counting()));
//			}
//
//			Iterator<String> it = resultMap.keySet().iterator();
//			while (it.hasNext()) {
//				String key = it.next();
//				for (int i = 0; i < 10; i++) {
//					JSONObject jsonObj = new JSONObject();
//					jsonObj.put("year", Integer.toString(1000 + i));
//					jsonObj.put("value", Math.random() * 100);
//					chartDataVo.addData(jsonObj);
//				}
//			}
//
//		}
//
//		return null;
//	}
	
	@Override
	protected ChartDataVo myGetData(DashboardWidgetVo widgetVo) {

		ChartDataVo chartDataVo = new ChartDataVo();
		chartDataVo.setGroupField("year");
		chartDataVo.setValueField("value");
		for (int i = 0; i < 10; i++) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("year", Integer.toString(1000 + i));
			jsonObj.put("value", Math.random() * 100);
			chartDataVo.addData(jsonObj);
		}

		return chartDataVo;
	}

	@Override
	public JSONObject getChartConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		return "ITSM";
	}

	class ProcessTaskResultHandler implements ResultHandler<ProcessTaskVo> {
		@Override
		public void handleResult(ResultContext resultContext) {
			// TODO Auto-generated method stub

		}

	}
}
