package codedriver.framework.process.workcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnFactory;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Service
public class WorkcenterHandler {

	@Autowired
	WorkcenterEsHandler workcenterEsService;
	
	public JSONObject doSearch(WorkcenterVo workcenterVo) {
		JSONObject returnObj = new JSONObject();
		//搜索es
		QueryResult result = workcenterEsService.searchTask(workcenterVo);;
		List<MultiAttrsObject> resultData = result.getData();
		//返回的数据重新加工
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		List<JSONObject> headerList = new ArrayList<JSONObject>();
		Map<String, IWorkcenterColumn> columnComponentMap = WorkcenterColumnFactory.columnComponentMap;
		if (!resultData.isEmpty()) {
            for (MultiAttrsObject el : resultData) {
            	JSONObject taskJson = new JSONObject();
            	taskJson.put("taskId", el.getId());
            	for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
            		IWorkcenterColumn column = entry.getValue();
            		taskJson.put(column.getName(), column.getValue(el));
            	}
            	//补充表单属性值
            	for(Object header : workcenterVo.getHeaderArray()) {
            		if(!taskJson.containsKey(header.toString())) {
            			taskJson.put(header.toString(),el.getString(header.toString()));
            		}
            	}
            	dataList.add(taskJson);
            }
        }
		//TODO 从新获取header，兼容表单
		returnObj.put("headerList", headerList);
		returnObj.put("dataList", dataList);
		returnObj.put("rowNum", result.getTotal());
		returnObj.put("pageSize", workcenterVo.getPageSize());
		returnObj.put("currentPage", workcenterVo.getCurrentPage());
		returnObj.put("pageCount", PageUtil.getPageCount(result.getTotal(), workcenterVo.getPageSize()));
		return returnObj;
	}
	
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		 String a =json.getString("a");
		 System.out.println(a);
		
	}
}
