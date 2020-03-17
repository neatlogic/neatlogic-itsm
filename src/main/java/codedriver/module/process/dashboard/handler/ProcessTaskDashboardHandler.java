package codedriver.module.process.dashboard.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.ChartDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

	@Override
	public String getName() {
		return "ITSM任务数据";
	}

	@Override
	protected ChartDataVo myGetData(DashboardWidgetVo widgetVo) {

		ChartDataVo chartDataVo = new ChartDataVo();
		chartDataVo.setLegendField("year");
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

}
