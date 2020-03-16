package codedriver.framework.process.workcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnFactory;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.framework.process.workcenter.condition.handler.ProcessTaskContentCondition;
import codedriver.framework.process.workcenter.condition.handler.ProcessTaskIdCondition;
import codedriver.framework.process.workcenter.condition.handler.ProcessTaskTitleCondition;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;
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
	
	/**
	 * 工单中心根据条件获取工单列表数据
	 * @param workcenterVo
	 * @return
	 */
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
		//多删
		ListIterator<WorkcenterTheadVo> it = theadList.listIterator();
		while(it.hasNext()) {
			WorkcenterTheadVo thead = it.next();
			if(thead.getType().equals(ProcessWorkcenterConditionType.COMMON.getValue())) {
				if(!columnComponentMap.containsKey(thead.getName())) {
					it.remove();
				}else {
					thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
				}
			}else {
				List<String> channelUuidList = workcenterVo.getChannelUuidList();
				if(CollectionUtils.isNotEmpty(channelUuidList)) {
					List<FormAttributeVo> formAttrList = formMapper.getFormAttributeListByChannelUuidList(channelUuidList);
					List<FormAttributeVo> theadFormList = formAttrList.stream().filter(attr->attr.getUuid().equals(thead.getName())).collect(Collectors.toList());
					if(CollectionUtils.isEmpty(theadFormList)){
						it.remove();
					}else {
						thead.setDisplayName(theadFormList.get(0).getLabel());
					}
				}
			}
		}
		//少补
		for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
    		IWorkcenterColumn column = entry.getValue();
    		if(CollectionUtils.isEmpty(theadList.stream().filter(data->column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
    			theadList.add(new WorkcenterTheadVo(column));
    		}
    	}

		if (!resultData.isEmpty()) {
            for (MultiAttrsObject el : resultData) {
            	JSONObject taskJson = new JSONObject();
            	taskJson.put("taskId", el.getId());
            	for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
            		IWorkcenterColumn column = entry.getValue();
            		taskJson.put(column.getName(), column.getValue(el));
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
	
	/**
	 * 根据单个关键字获取过滤选项
	 * @param keyword
	 * @return
	 */
	public static JSONArray getKeywordOption(IWorkcenterCondition condition, String keyword,Integer pageSize) {
		JSONArray returnArray = new JSONArray();
		WorkcenterVo workcenter = getKeywordCondition(condition,keyword);
		workcenter.setPageSize(pageSize);
		List<MultiAttrsObject> titleData = WorkcenterEsHandler.searchTask(workcenter).getData();
		if (!titleData.isEmpty()) {
			JSONObject titleObj = new JSONObject();
			JSONArray titleDataList = new JSONArray();
            for (MultiAttrsObject titleEl : titleData) {
            	titleDataList.add(WorkcenterColumnFactory.getHandler(condition.getName()).getValue(titleEl));
            }
            titleObj.put("dataList", titleDataList);
            titleObj.put("value", condition.getName());
            titleObj.put("text",condition.getDisplayName());
            returnArray.add(titleObj);
		}
		return returnArray;
	}
	
	/**
	 * 根据关键字获取过滤选项
	 * @param keyword
	 * @return
	 */
	public static JSONArray getKeywordOptions(String keyword,Integer pageSize){
		//搜索标题
		JSONArray returnArray = getKeywordOption(new ProcessTaskTitleCondition(),keyword,pageSize);
		//搜索ID
		returnArray.addAll(getKeywordOption(new ProcessTaskIdCondition(),keyword,pageSize));
		//搜索内容
		returnArray.addAll(getKeywordOption(new ProcessTaskContentCondition(),keyword,pageSize));
		return returnArray;
	}
	
	/**
	 * 拼接关键字过滤选项
	 * @param type 搜索内容类型
	 * @return 
	 */
	private static WorkcenterVo getKeywordCondition(IWorkcenterCondition condition,String keyword) {
		JSONObject  searchObj = new JSONObject();
		JSONArray conditionGroupList = new JSONArray();
		JSONObject conditionGroup = new JSONObject();
		JSONArray conditionList = new JSONArray();
		JSONObject conditionObj = new JSONObject();
		conditionObj.put("name", String.format("%s#%s",condition.getType(),condition.getName()));
		JSONArray valueList = new JSONArray();
		valueList.add(keyword);
		conditionObj.put("valueList", valueList);
		conditionObj.put("expression", ProcessExpression.LIKE.getExpression());
		conditionList.add(conditionObj);
		conditionGroup.put("conditionList", conditionList);
		conditionGroupList.add(conditionGroup);
		searchObj.put("conditionGroupList", conditionGroupList);
		
		return new WorkcenterVo(searchObj);
		
	}
}
