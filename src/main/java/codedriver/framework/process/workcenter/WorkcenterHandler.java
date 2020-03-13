package codedriver.framework.process.workcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnFactory;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.module.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

public class WorkcenterHandler {
	static WorkcenterMapper workcenterMapper;
	@Autowired
	public void setWorkcenterMapper(WorkcenterMapper _workcenterMapper) {
		workcenterMapper = _workcenterMapper;
	}
	
	public static JSONObject doSearch(WorkcenterVo workcenterVo) {
		JSONObject returnObj = new JSONObject();
		//搜索es
		QueryResult result = WorkcenterEsHandler.searchTask(workcenterVo);;
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
            	for(WorkcenterTheadVo header : workcenterMapper.getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(),UserContext.get().getUserId()))) {
            		if(!taskJson.containsKey(header.getName())) {
            			taskJson.put(header.getName(),el.getString(header.getName()));
            		}
            	}
            	dataList.add(taskJson);
            }
        }
		//TODO 从新获取header，兼容表单
		returnObj.put("theadList", headerList);
		returnObj.put("tbodyList", dataList);
		returnObj.put("rowNum", result.getTotal());
		returnObj.put("pageSize", workcenterVo.getPageSize());
		returnObj.put("currentPage", workcenterVo.getCurrentPage());
		returnObj.put("pageCount", PageUtil.getPageCount(result.getTotal(), workcenterVo.getPageSize()));
		return returnObj;
	}
}
