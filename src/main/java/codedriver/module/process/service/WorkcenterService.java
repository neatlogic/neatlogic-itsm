package codedriver.module.process.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;
import com.techsure.multiattrsearch.util.ESQueryUtil;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.condition.core.IWorkcenterCondition;
import codedriver.framework.process.condition.core.WorkcenterConditionFactory;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessWorkcenterConditionModel;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.condition.ConditionGroupRelVo;
import codedriver.framework.process.dto.condition.ConditionGroupVo;
import codedriver.framework.process.dto.condition.ConditionRelVo;
import codedriver.framework.process.dto.condition.ConditionVo;
import codedriver.framework.process.exception.workcenter.WorkcenterConditionException;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnFactory;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.elasticsearch.core.WorkcenterEsHandlerBase;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.condition.handler.ProcessTaskContentCondition;
import codedriver.module.process.condition.handler.ProcessTaskIdCondition;
import codedriver.module.process.condition.handler.ProcessTaskTitleCondition;
@Service
public class WorkcenterService {
	Logger logger = LoggerFactory.getLogger(WorkcenterService.class);
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	ProcessTaskService processTaskService;
	@Autowired
	FormMapper formMapper;
	
	
	/**
	 *   搜索工单
	 * @param workcenterVo
	 * @return 
	 */
	private  QueryResult searchTask(WorkcenterVo workcenterVo){
		String selectColumn = "*";
		String where = assembleWhere(workcenterVo);
		String orderBy = "order by createTime desc";
		String sql = String.format("select %s from techsure %s %s limit %d,%d", selectColumn,where,orderBy,workcenterVo.getStartNum(),workcenterVo.getPageSize());
		return ESQueryUtil.query(ElasticSearchPoolManager.getObjectPool(WorkcenterEsHandlerBase.POOL_NAME), sql);
	}
	/**
	 * 工单中心根据条件获取工单列表数据
	 * @param workcenterVo
	 * @return
	 */
	public JSONObject doSearch(WorkcenterVo workcenterVo) {
		JSONObject returnObj = new JSONObject();
		//搜索es
		QueryResult result = searchTask(workcenterVo);
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
			if(thead.getType().equals(ProcessFieldType.COMMON.getValue())) {
				if(!columnComponentMap.containsKey(thead.getName())) {
					it.remove();
				}else {
					thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
					thead.setClassName(columnComponentMap.get(thead.getName()).getClassName());
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
            	taskJson.put("taskid", el.getId());
            	for (Map.Entry<String, IWorkcenterColumn> entry : columnComponentMap.entrySet()) {
            		IWorkcenterColumn column = entry.getValue();
            		taskJson.put(column.getName(), column.getValue(el));
            	}
            	//route 供前端跳转路由信息
            	JSONObject routeJson = new JSONObject();
            	routeJson.put("taskid", el.getId());
            	taskJson.put("route", routeJson);
            	//action 操作
            	taskJson.put("action", getStepAction(el));
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
	
	private Object getStepAction(MultiAttrsObject el) {
		JSONArray actionArray = new JSONArray();
		JSONObject commonJson = (JSONObject) el.getJSON(ProcessFieldType.COMMON.getValue());
		if(commonJson == null) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray currentStepArray = (JSONArray) commonJson.getJSONArray(ProcessWorkcenterField.CURRENT_STEP.getValue());
		if(CollectionUtils.isEmpty(currentStepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		for(Object currentStepObj: currentStepArray) {
			JSONObject currentStepJson = (JSONObject)currentStepObj;
			Long stepId = currentStepJson.getLong("id");
			String stepName = currentStepJson.getString("name");
			String stepStatus = currentStepJson.getString("status");
			if(StringUtils.isNotBlank(stepStatus)&&(stepStatus.equals(ProcessTaskStatus.PENDING.getValue())||stepStatus.equals(ProcessTaskStatus.RUNNING.getValue())||stepStatus.equals(ProcessTaskStatus.DRAFT.getValue()))) {
				List<String> actionList = new ArrayList<String>();
				try {
					actionList = processTaskService.getProcessTaskStepActionList(Long.valueOf(el.getId()), currentStepJson.getLong("id"));
				}catch(Exception ex) {
					logger.error(ex.getMessage(),ex);
				}
				if(actionList.contains(ProcessTaskStepAction.COMPLETESUBTASK.getValue())||
						actionList.contains(ProcessTaskStepAction.COMPLETE.getValue())||
						actionList.contains(ProcessTaskStepAction.START.getValue())||
						actionList.contains(ProcessTaskStepAction.STARTPROCESS.getValue())) {
					JSONObject configJson = new JSONObject();
					configJson.put("taskid", el.getId());
					configJson.put("stepid", stepId);
					configJson.put("stepName", stepName);
					JSONObject actionJson = new JSONObject();
					actionJson.put("name", "handle");
					actionJson.put("text", String.format("处理:%s", stepName));
					actionJson.put("config", configJson);
					actionArray.add(actionJson);
				}
				if(actionList.contains(ProcessTaskStepAction.ABORT.getValue())) {
					JSONObject actionJson = new JSONObject();
					JSONObject configJson = new JSONObject();
					configJson.put("taskid", el.getId());
					configJson.put("interfaceurl", "api/rest/processtask/abort?processTaskId="+el.getId());
					actionJson.put("name", ProcessTaskStepAction.ABORT.getValue());
					actionJson.put("text", ProcessTaskStepAction.ABORT.getText());
					actionJson.put("config", configJson);
					actionArray.add(actionJson);
				}
			}
		}
		return actionArray;
	}
	
	/**
	 * 工单中心根据条件获取工单列表数据
	 * @param workcenterVo
	 * @return
	 */
	public Integer doSearchCount(WorkcenterVo workcenterVo) {
		//搜索es
		QueryResult result = searchTask(workcenterVo);;
		return result.getTotal();
	}
	
	/**
	 * 根据关键字获取所有过滤选项
	 * @param keyword
	 * @return
	 */
	public JSONArray getKeywordOptions(String keyword,Integer pageSize){
		//搜索标题
		JSONArray returnArray = getKeywordOption(new ProcessTaskTitleCondition(),keyword,pageSize);
		//搜索ID
		returnArray.addAll(getKeywordOption(new ProcessTaskIdCondition(),keyword,pageSize));
		//搜索内容
		returnArray.addAll(getKeywordOption(new ProcessTaskContentCondition(),keyword,pageSize));
		return returnArray;
	}
	
	/**
	 * 根据单个关键字获取过滤选项
	 * @param keyword
	 * @return
	 */
	private JSONArray getKeywordOption(IWorkcenterCondition condition, String keyword,Integer pageSize) {
		JSONArray returnArray = new JSONArray();
		WorkcenterVo workcenter = getKeywordCondition(condition,keyword);
		workcenter.setPageSize(pageSize);
		List<MultiAttrsObject> titleData = searchTask(workcenter).getData();
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
	 * 拼接关键字过滤选项
	 * @param type 搜索内容类型
	 * @return 
	 */
	private WorkcenterVo getKeywordCondition(IWorkcenterCondition condition,String keyword) {
		JSONObject  searchObj = new JSONObject();
		JSONArray conditionGroupList = new JSONArray();
		JSONObject conditionGroup = new JSONObject();
		JSONArray conditionList = new JSONArray();
		JSONObject conditionObj = new JSONObject();
		conditionObj.put("name", condition.getName());
		conditionObj.put("type", condition.getType());
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
	
	/**
	 * 拼接where条件
	 * @param workcenterVo
	 * @return
	 */
	private static String assembleWhere(WorkcenterVo workcenterVo) {
		Map<String,String> groupRelMap = new HashMap<String,String>();
		StringBuilder whereSb = new StringBuilder();
		whereSb.append(" where ");
		List<ConditionGroupRelVo> groupRelList = workcenterVo.getConditionGroupRelList();
		if(CollectionUtils.isNotEmpty(groupRelList)) {
			//将group 以连接表达式 存 Map<fromUuid_toUuid,joinType> 
			for(ConditionGroupRelVo groupRel : groupRelList) {
				groupRelMap.put(groupRel.getFrom()+"_"+groupRel.getTo(), groupRel.getJoinType());
			}
		}
		List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
		if(CollectionUtils.isEmpty(groupList)) {
			return "";
		}
		String fromGroupUuid = null;
		String toGroupUuid = groupList.get(0).getUuid();
		for(ConditionGroupVo group : groupList) {
			Map<String,String> conditionRelMap = new HashMap<String,String>();
			if(fromGroupUuid != null) {
				toGroupUuid = group.getUuid();
				whereSb.append(groupRelMap.get(fromGroupUuid+"_"+toGroupUuid));
			}
			whereSb.append("(");
			List<ConditionRelVo> conditionRelList = group.getConditionRelList();
			if(CollectionUtils.isNotEmpty(conditionRelList)) {
				//将condition 以连接表达式 存 Map<fromUuid_toUuid,joinType> 
				for(ConditionRelVo conditionRel : conditionRelList) {
					conditionRelMap.put(conditionRel.getFrom()+"_"+conditionRel.getTo(),conditionRel.getJoinType());
				}
			}
			List<ConditionVo> conditionList = group.getConditionList();
			String fromConditionUuid = null;
			String toConditionUuid = conditionList.get(0).getUuid();
			for(ConditionVo condition : conditionList) {
				if(fromConditionUuid != null) {
					toConditionUuid = condition.getUuid();
					whereSb.append(conditionRelMap.get(fromConditionUuid+"_"+toConditionUuid));
				}
				Object value = condition.getValueList().get(0);
				IWorkcenterCondition workcenterCondition = WorkcenterConditionFactory.getHandler(condition.getName());
				//Date 类型过滤条件特殊处理
				if(workcenterCondition != null && workcenterCondition.getHandler(ProcessWorkcenterConditionModel.SIMPLE.getValue()).equals(ProcessFormHandlerType.DATE.toString())){
					JSONArray dateJSONArray = JSONArray.parseArray(JSON.toJSONString(condition.getValueList()));
					if(CollectionUtils.isNotEmpty(dateJSONArray)) {
						JSONObject dateValue = JSONObject.parseObject(dateJSONArray.get(0).toString());
						SimpleDateFormat format = new SimpleDateFormat(TimeUtil.TIME_FORMAT);
						String startTime = StringUtils.EMPTY;
						String endTime = StringUtils.EMPTY;
						String expression = condition.getExpression();
						if(dateValue.containsKey(ProcessWorkcenterField.STARTTIME.getValue())) {
							startTime = format.format(new Date(dateValue.getLong(ProcessWorkcenterField.STARTTIME.getValue())));
							endTime = format.format(new Date(dateValue.getLong(ProcessWorkcenterField.ENDTIME.getValue())));
						}else {
							startTime = TimeUtil.timeTransfer(dateValue.getInteger("timeRange"), dateValue.getString("timeUnit"));
							endTime = TimeUtil.timeNow();
						}
						if(StringUtils.isEmpty(startTime)) {
							expression = ProcessExpression.LESSTHAN.getExpression();
							startTime = endTime;
						}else if(StringUtils.isEmpty(endTime)) {
							expression = ProcessExpression.GREATERTHAN.getExpression();
						}
						whereSb.append(String.format(ProcessExpression.getExpressionEs(expression),ProcessWorkcenterField.getConditionValue(condition.getName()),startTime,endTime));
					}else {
						throw new WorkcenterConditionException(condition.getName());
					}
				}else {
					if(condition.getValueList().size()>1) {
						value = String.join("','",condition.getValueList());
					}
					whereSb.append(String.format(ProcessExpression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(condition.getName()),String.format("'%s'",  value)));
				}
				fromConditionUuid = toConditionUuid;
			}
			
			whereSb.append(")");
			fromGroupUuid = toGroupUuid;
		}
		return whereSb.toString();
	}

}
