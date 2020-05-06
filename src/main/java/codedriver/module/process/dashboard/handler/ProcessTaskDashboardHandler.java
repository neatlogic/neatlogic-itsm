package codedriver.module.process.dashboard.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.QueryResultSet;

import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
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
	protected JSONArray myGetData(DashboardWidgetVo widgetVo) {

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
	public JSONObject getChartConfig() {
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
