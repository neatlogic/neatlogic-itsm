package codedriver.module.process.workcenter;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.workcenter.dto.WorkcenterVo;

public class WorkcenterService {

	public JSONObject doSearch(WorkcenterVo workcenterVo) {
		JSONObject returnObj = new JSONObject();
		//
		
		
		
		returnObj.put("headerList", null);
		returnObj.put("dataList", null);
		returnObj.put("rowNum", null);
		returnObj.put("pageSize", null);
		returnObj.put("currentPage", null);
		returnObj.put("pageCount", null);
		return returnObj;
	}
}
