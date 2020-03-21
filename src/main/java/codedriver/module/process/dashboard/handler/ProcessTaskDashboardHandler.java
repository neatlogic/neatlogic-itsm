package codedriver.module.process.dashboard.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.dto.ChartDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.module.process.service.WorkcenterService;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

	@Autowired
	private WorkcenterService workcenterService;

	@Override
	public String getName() {
		return "processtask";
	}

	/*
	 * 
	 * { "groupField":"urgency", "aggregate":"count",
	 * "subGroupField":"","valueField":"", "displaycolumn":["owner","title"],
	 * "sort":"createTime", "limit":10 }
	 */

	@Override
	protected JSONArray myGetData(DashboardWidgetVo widgetVo) {

		DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());

		if (chart != null) {
			// JSONObject resultObj = workcenterService.doSearch(new WorkcenterVo());
			// JSONArray dataList = resultObj.getJSONArray("tbodyList");
			JSONArray dataList = new JSONArray();
			String[] workers = new String[] { "chenqw", "admin", "wangtc", "wenhb", "wugq" };
			String[] urgencys = new String[] {"紧急","普通"};
			for (int i = 0; i < 100; i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("worker", workers[(int) (Math.random() * 100 % 5)]);
				jsonObj.put("processTaskId", Math.random() * 100);
				jsonObj.put("urgency", urgencys[(int) (Math.random() * 100 % 2)]);
				dataList.add(jsonObj);
			}
			if (CollectionUtils.isNotEmpty(dataList)) {
				return chart.getData(dataList, widgetVo.getChartConfigObj());
			}
		}

		return null;
	}

	protected ChartDataVo myGetData2(DashboardWidgetVo widgetVo) {

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

	@Override
	public String getDisplayName() {
		return "ITSM任务数据";
	}

	@Override
	public String getIcon() {
		return "xx-icon";
	}
}
