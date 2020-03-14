package codedriver.framework.process.workcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnFactory;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.module.process.dto.FormAttributeVo;
import codedriver.module.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;
@Service
public class WorkcenterHandler {
	static WorkcenterMapper workcenterMapper;
	static FormMapper formMapper;
	@Autowired
	public void setWorkcenterMapper(WorkcenterMapper _workcenterMapper) {
		workcenterMapper = _workcenterMapper;
	}
	
	@Autowired
	public void setFormMapper(FormMapper _formMapper) {
		formMapper = _formMapper;
	}
	
	public static JSONObject doSearch(WorkcenterVo workcenterVo) {
		JSONObject returnObj = new JSONObject();
		//搜索es
		QueryResult result = WorkcenterEsHandler.searchTask(workcenterVo);;
		List<MultiAttrsObject> resultData = result.getData();
		//返回的数据重新加工
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		Map<String, IWorkcenterColumn> columnComponentMap = WorkcenterColumnFactory.columnComponentMap;
		//获取用户历史自定义theadList
		List<WorkcenterTheadVo> theadList = workcenterMapper.getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(),UserContext.get().getUserId()));
		//矫正theadList 或存在表单属性或固定字段增删
		//固定字段矫正 多删，少补
		ListIterator<WorkcenterTheadVo> it = theadList.listIterator();
		while(it.hasNext()) {
			WorkcenterTheadVo thead = it.next();
			if(!columnComponentMap.containsKey(thead.getName())) {
				it.remove();
			}
		}
		for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
    		IWorkcenterColumn column = entry.getValue();
    		if(CollectionUtils.isEmpty(theadList.stream().filter(data->column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
    			theadList.add(new WorkcenterTheadVo(column));
    		}
    	}
		//表单字段矫正 多删
		List<String> channelUuidList = workcenterVo.getChannelUuidList();
		if(CollectionUtils.isNotEmpty(channelUuidList)) {
			List<FormAttributeVo>  formAttrList = formMapper.getFormAttributeListByChannelList(channelUuidList);
			
		}
		
		
		
		
		
		
		
		
		
		if (!resultData.isEmpty()) {
            for (MultiAttrsObject el : resultData) {
            	JSONObject taskJson = new JSONObject();
            	taskJson.put("taskId", el.getId());
            	for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
            		IWorkcenterColumn column = entry.getValue();
            		taskJson.put(column.getName(), column.getValue(el));
            	}
            	//补充表单属性值
            	for(WorkcenterTheadVo header : theadList) {
            		if(!taskJson.containsKey(header.getName())) {
            			taskJson.put(header.getName(),el.getString(header.getName()));
            		}
            		header.setIsShow(1);
            	}
            	dataList.add(taskJson);
            }
        }

		returnObj.put("theadList", theadList);
		returnObj.put("tbodyList", dataList);
		returnObj.put("rowNum", result.getTotal());
		returnObj.put("pageSize", workcenterVo.getPageSize());
		returnObj.put("currentPage", workcenterVo.getCurrentPage());
		returnObj.put("pageCount", PageUtil.getPageCount(result.getTotal(), workcenterVo.getPageSize()));
		return returnObj;
	}
}
