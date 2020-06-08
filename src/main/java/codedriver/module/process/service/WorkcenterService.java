package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.MultiAttrsQuery;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryParser;
import com.techsure.multiattrsearch.query.QueryResult;
import com.techsure.multiattrsearch.util.ESQueryUtil;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.condition.ConditionGroupRelVo;
import codedriver.framework.process.dto.condition.ConditionGroupVo;
import codedriver.framework.process.dto.condition.ConditionRelVo;
import codedriver.framework.process.dto.condition.ConditionVo;
import codedriver.framework.process.elasticsearch.core.ProcessTaskEsHandlerBase;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.condition.handler.ProcessTaskIdCondition;
import codedriver.module.process.condition.handler.ProcessTaskTitleCondition;
@Service
public class WorkcenterService {
	Logger logger = LoggerFactory.getLogger(WorkcenterService.class);
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;
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
		String meWillDoCondition = getMeWillDoCondition(workcenterVo);
		if(StringUtils.isNotBlank(meWillDoCondition)) {
			if(StringUtils.isBlank(where)) {
				where = " where " + getMeWillDoCondition(workcenterVo);
			}else {
				where = where + " and " + getMeWillDoCondition(workcenterVo);
			}
		}
		String orderBy = "order by common.starttime desc";
		String sql = String.format("select %s from %s %s %s limit %d,%d", selectColumn,TenantContext.get().getTenantUuid(),where,orderBy,workcenterVo.getStartNum(),workcenterVo.getPageSize());
		return ESQueryUtil.query(ElasticSearchPoolManager.getObjectPool(ProcessTaskEsHandlerBase.POOL_NAME), sql);
	}
	
	/**
	 * 附加我的待办条件
	 * @return
	 */
	private String getMeWillDoCondition(WorkcenterVo workcenterVo) {
		String meWillDoSql = StringUtils.EMPTY;
		if(workcenterVo.getIsMeWillDo() == 1) {
			//status
			List<String> statusList = Arrays.asList(ProcessTaskStatus.RUNNING.getValue()).stream().map(object -> object.toString()).collect(Collectors.toList());
			String statusSql = String.format(Expression.INCLUDE.getExpressionEs(), ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STATUS.getValue()),String.format(" '%s' ", String.join("','",statusList)));
			//common.step.filtstatus
			List<String> stepStatusList = Arrays.asList(ProcessTaskStatus.PENDING.getValue(),ProcessTaskStatus.RUNNING.getValue()).stream().map(object -> object.toString()).collect(Collectors.toList());
			String stepStatusSql = String.format(Expression.INCLUDE.getExpressionEs(), ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue())+".filtstatus",String.format(" '%s' ", String.join("','",stepStatusList)));
			//common.step.usertypelist.userlist
			List<String> userList = new ArrayList<String>();
			userList.add(GroupSearch.USER.getValuePlugin()+UserContext.get().getUserUuid());
			//如果是待处理状态，则需额外匹配角色和组
			UserVo userVo = userMapper.getUserByUuid(UserContext.get().getUserUuid());
			if(userVo != null) {
				List<String> teamList = userVo.getTeamNameList();
				if(CollectionUtils.isNotEmpty(teamList)) {
					for(String team : teamList) {
						userList.add(GroupSearch.TEAM.getValuePlugin()+team);
					}
				}
				List<String> roleUuidList = userVo.getRoleUuidList();
				if(CollectionUtils.isNotEmpty(roleUuidList)) {
					for(String roleUuid : roleUuidList) {
						userList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
					}
				}
			}

			String userListSql = String.format(Expression.INCLUDE.getExpressionEs(), ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP_USER.getValue()),
					String.format(" '%s' ", String.join("','",userList)));
			meWillDoSql = String.format(" ([ %s and %s and %s ])", statusSql,stepStatusSql,userListSql) ;
		}
		return meWillDoSql;
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
		Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
		//获取用户历史自定义theadList
		List<WorkcenterTheadVo> theadList = workcenterMapper.getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(),UserContext.get().getUserUuid()));
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
		for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
    		IProcessTaskColumn column = entry.getValue();
    		if(column.getIsShow()&&CollectionUtils.isEmpty(theadList.stream().filter(data->column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
    			theadList.add(new WorkcenterTheadVo(column));
    		}
    	}

		if (!resultData.isEmpty()) {
            for (MultiAttrsObject el : resultData) {
            	JSONObject taskJson = new JSONObject();
            	taskJson.put("taskid", el.getId());
            	for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            		IProcessTaskColumn column = entry.getValue();
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
	
	/**
	 * 工单中心 获取操作按钮
	 * @param MultiAttrsObject el
	 * @return
	 */
	private Object getStepAction(MultiAttrsObject el) {
		JSONArray actionArray = new JSONArray();
		JSONObject commonJson = (JSONObject) el.getJSON(ProcessFieldType.COMMON.getValue());
		Boolean isHasAbort = false;
		Boolean isHasRecover = false;
		Boolean isHasUrge = false;
		if(commonJson == null) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		//TODO 临时测试
		JSONArray stepArray = null;
		try {
		   stepArray = (JSONArray) commonJson.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		}catch(Exception ex){
			return "";
		}
		String processTaskStatus = commonJson.getString(ProcessWorkcenterField.STATUS.getValue());
		if(CollectionUtils.isEmpty(stepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONObject handleActionJson = new JSONObject();
		JSONArray handleArray = new JSONArray();
		for(Object stepObj: stepArray) {
			JSONObject stepJson = (JSONObject)stepObj;
			Long stepId = stepJson.getLong("id");
			String stepName = stepJson.getString("name");
			String stepStatus = stepJson.getString("status");		
			Integer isActive =stepJson.getInteger("isactive");
			if((ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)||ProcessTaskStatus.DRAFT.getValue().equals(processTaskStatus)||ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus))
					&&((ProcessTaskStatus.PENDING.getValue().equals(stepStatus)&&isActive == 1)||ProcessTaskStatus.RUNNING.getValue().equals(stepStatus)||ProcessTaskStatus.DRAFT.getValue().equals(stepStatus))) {		
				List<String> actionList = new ArrayList<String>();
				try {
					actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(Long.valueOf(el.getId()), stepJson.getLong("id"),new ArrayList<String>(){
						private static final long serialVersionUID = 1L;
					{
						add(ProcessTaskStepAction.WORK.getValue());
						add(ProcessTaskStepAction.ABORT.getValue());
						add(ProcessTaskStepAction.RECOVER.getValue());
						add(ProcessTaskStepAction.URGE.getValue());
						}});
				}catch(Exception ex) {
					logger.error(ex.getMessage(),ex);
				}
				
				if(actionList.contains(ProcessTaskStepAction.WORK.getValue())) { 
				JSONObject configJson = new JSONObject();
					configJson.put("taskid", el.getId());
					configJson.put("stepid", stepId);
					configJson.put("stepName", stepName);
					JSONObject actionJson = new JSONObject();
					actionJson.put("name", "handle");
					actionJson.put("text", stepName);
					actionJson.put("config", configJson);
					handleArray.add(actionJson);
				}
				if(actionList.contains(ProcessTaskStepAction.ABORT.getValue())) {
					isHasAbort = true; 
				}
				if(actionList.contains(ProcessTaskStepAction.RECOVER.getValue())) {
					isHasRecover = true; 
				}
				if(actionList.contains(ProcessTaskStepAction.URGE.getValue())) {
					isHasUrge = true; 
				}
			}
		}
		
		handleActionJson.put("name", "handle");
		handleActionJson.put("text", "处理");
		handleActionJson.put("sort", 2);
		if(CollectionUtils.isNotEmpty(handleArray)) {
			handleActionJson.put("handleList", handleArray);
			handleActionJson.put("isEnable", 1);
		}else {
			handleActionJson.put("isEnable", 0);
		} 
		
		actionArray.add(handleActionJson);
		//abort|recover
		if(isHasAbort||isHasRecover) {
			if(isHasAbort) {
				JSONObject abortActionJson = new JSONObject();
				abortActionJson.put("name", ProcessTaskStepAction.ABORT.getValue());
				abortActionJson.put("text", ProcessTaskStepAction.ABORT.getText());
				abortActionJson.put("sort", 2);
				JSONObject configJson = new JSONObject();
				configJson.put("taskid", el.getId());
				configJson.put("interfaceurl", "api/rest/processtask/abort?processTaskId="+el.getId());
				abortActionJson.put("config", configJson);
				abortActionJson.put("isEnable", 1);
				actionArray.add(abortActionJson);
			}else {
				JSONObject recoverActionJson = new JSONObject();
				recoverActionJson.put("name", ProcessTaskStepAction.RECOVER.getValue());
				recoverActionJson.put("text", ProcessTaskStepAction.RECOVER.getText());
				recoverActionJson.put("sort", 2);
				JSONObject configJson = new JSONObject();
				configJson.put("taskid", el.getId());
				configJson.put("interfaceurl", "api/rest/processtask/recover?processTaskId="+el.getId());
				recoverActionJson.put("config", configJson);
				recoverActionJson.put("isEnable", 1);
				actionArray.add(recoverActionJson);
			}
		}else {
			JSONObject abortActionJson = new JSONObject();
			abortActionJson.put("name", ProcessTaskStepAction.ABORT.getValue());
			abortActionJson.put("text", ProcessTaskStepAction.ABORT.getText());
			abortActionJson.put("sort", 2);
			abortActionJson.put("isEnable", 0);
			actionArray.add(abortActionJson);
		}
		
		//催办
		JSONObject urgeActionJson = new JSONObject();
		urgeActionJson.put("name", ProcessTaskStepAction.URGE.getValue());
		urgeActionJson.put("text", ProcessTaskStepAction.URGE.getText());
		urgeActionJson.put("sort", 3);
		if(isHasUrge) {
			JSONObject configJson = new JSONObject();
			configJson.put("taskid", el.getId());
			configJson.put("interfaceurl", "api/rest/processtask/urge?processTaskId="+el.getId());
			urgeActionJson.put("config", configJson);
			urgeActionJson.put("isEnable", 1);
		}else {
			urgeActionJson.put("isEnable", 0);
		}

		actionArray.add(urgeActionJson);
		
		
		actionArray.sort(Comparator.comparing(obj-> ((JSONObject) obj).getInteger("sort")));
		return actionArray;
	}
	
	/**
	 * 工单中心根据条件获取工单列表数据
	 * @param workcenterVo
	 * @return
	 */
	public Integer doSearchCount(WorkcenterVo workcenterVo) {
		//搜索es
		QueryResult result = searchTask(workcenterVo);
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
		return returnArray;
	}
	
	/**
	 * 根据单个关键字获取过滤选项
	 * @param keyword
	 * @return
	 */
	private JSONArray getKeywordOption(IProcessTaskCondition condition, String keyword,Integer pageSize) {
		JSONArray returnArray = new JSONArray();
		WorkcenterVo workcenter = getKeywordCondition(condition,keyword);
		workcenter.setPageSize(pageSize);
		List<MultiAttrsObject> dataList = searchTask(workcenter).getData();
		if (!dataList.isEmpty()) {
			JSONObject titleObj = new JSONObject();
			JSONArray titleDataList = new JSONArray();
            for (MultiAttrsObject titleEl : dataList) {
            	IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(condition.getName());
            	if(column == null) {
            		continue;
            	}
            	titleDataList.add(column.getValue(titleEl));
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
	private WorkcenterVo getKeywordCondition(IProcessTaskCondition condition,String keyword) {
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
		conditionObj.put("expression", Expression.LIKE.getExpression());
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
	@SuppressWarnings("unchecked")
	private static String assembleWhere(WorkcenterVo workcenterVo) {
		Map<String,String> groupRelMap = new HashMap<String,String>();
		StringBuilder whereSb = new StringBuilder();
		whereSb.append(" where (");
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
			//按es and 组合,数组元素之间用or
			JSONArray conditionRelationArray = new JSONArray();
			ConditionVo fromCondition = null;
			ArrayList<ConditionVo> andConditionList = new ArrayList<ConditionVo>();
			//统计 common.step 开头的condition count
			int nestedBasisCount = 0;
			for(int i = 0;i<conditionList.size();i++) {
				ConditionVo condition = conditionList.get(i);
				if(!condition.getType().equals("form")&&ProcessWorkcenterField.getConditionValue(condition.getName()).startsWith(ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue()))) {
					nestedBasisCount++;
				}
				if(fromCondition == null) {
					fromCondition = condition;
					if(conditionList.size() == 1) {
						JSONObject conditionRelationJson = new JSONObject();
						andConditionList.add(condition);
						conditionRelationJson.put("list", andConditionList);
						conditionRelationJson.put("isNested", false);
						conditionRelationArray.add(conditionRelationJson);
						andConditionList = new ArrayList<ConditionVo>();
					}
					andConditionList.add(condition);
					continue;
				}
				String conditionUuid = condition.getUuid();
				String conditionType = conditionRelMap.get(fromCondition.getUuid()+"_"+conditionUuid);
				if(i != conditionList.size()-1&&conditionType.equals("and")) {
					andConditionList.add(condition);
				}
				if(conditionType.equals("or")) {//如果是or 则另新建数组元素
					JSONObject conditionRelationJson = new JSONObject();
					conditionRelationJson.put("list", andConditionList);
					conditionRelationJson.put("isNested", false);
					conditionRelationArray.add(conditionRelationJson);
					andConditionList = new ArrayList<ConditionVo>();
					if(i != conditionList.size()-1) {
						andConditionList.add(condition);
					}
				}
				if(i == conditionList.size()-1) {
					andConditionList.add(condition);
					Collections.sort(andConditionList, new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2) {
							try {
								ConditionVo obj1 = (ConditionVo) o1;
								ConditionVo obj2 = (ConditionVo) o2;
								return obj1.getConditionValue().compareTo(obj2.getConditionValue());
							} catch (Exception ex) {

							}
							return 0;
						}
					});
					ArrayList<ConditionVo> tmpConditionList = new ArrayList<ConditionVo>();
					tmpConditionList.addAll(andConditionList);
					JSONObject conditionRelationJson = new JSONObject();
					conditionRelationJson.put("list", tmpConditionList);
					if(nestedBasisCount >1) {
						conditionRelationJson.put("isNested", true);
					}else {
						conditionRelationJson.put("isNested", false);
					}
					conditionRelationArray.add(conditionRelationJson);
					andConditionList = new ArrayList<ConditionVo>();
					nestedBasisCount = 0;
				}
				fromCondition = condition;
			}
			for(int orIndex =0;orIndex<conditionRelationArray.size();orIndex++) {
				Object conditionRelation = conditionRelationArray.get(orIndex);
				JSONObject conditionRelationJson  = (JSONObject) JSONObject.toJSON(conditionRelation);
				ArrayList<ConditionVo> andConditionTmpList = (ArrayList<ConditionVo>) conditionRelationJson.get("list");
				ArrayList<ConditionVo> formConditionList = new ArrayList<ConditionVo>();
				Boolean isNested = (Boolean) conditionRelationJson.get("isNested");
				if(isNested) {
					whereSb.append(" [");
				}
				
				for(int andIndex =0;andIndex<andConditionTmpList.size();andIndex++) {
					ConditionVo condition = andConditionTmpList.get(andIndex);
					IProcessTaskCondition workcenterCondition = ProcessTaskConditionFactory.getHandler(condition.getName());
					if(condition.getType().equals("form")) {
						formConditionList.add(condition);
					}else {
						String conditionWhere = workcenterCondition.getEsWhere(andConditionTmpList,andIndex);
						whereSb.append(conditionWhere);
						if(andIndex != andConditionTmpList.size()-1&&!andConditionTmpList.get(andIndex+1).getType().equals("form")) {
							whereSb.append(" and ");
						}
					}
				}
				if(isNested) {
					whereSb.append(" ]");
				}
				//form
				if(formConditionList.size()>0 && andConditionTmpList.size() != formConditionList.size()) {
					whereSb.append(" and ");
				}
				for(int formIndex =0;formIndex<formConditionList.size();formIndex++) {
					IProcessTaskCondition workcenterCondition = ProcessTaskConditionFactory.getHandler("form");
					String conditionWhere = workcenterCondition.getEsWhere(formConditionList,formIndex);
					whereSb.append(conditionWhere);
					if(formIndex != formConditionList.size()-1) {
						whereSb.append(" and ");
					}
				}
				if(orIndex != conditionRelationArray.size()-1) {
					whereSb.append(" or ");
				}
			}

			whereSb.append(")");
			fromGroupUuid = toGroupUuid;
		}
		return whereSb.toString()+")";
	}
	
	/**
	 *   流式搜索工单
	 * @param workcenterVo
	 * @return 
	 */
	public  QueryResultSet searchTaskIterate(WorkcenterVo workcenterVo){
		JSONArray resultColumnArray = workcenterVo.getResultColumnList();
		String selectColumn = "*";
		
		if(!CollectionUtils.isEmpty(resultColumnArray)) {
			List<String> columnResultList = new ArrayList<String>();
			for(Object column:resultColumnArray) {
				columnResultList.add(ProcessWorkcenterField.getConditionValue(column.toString()));
				selectColumn = String.join(",", columnResultList);
			}
		}
		
		String where = assembleWhere(workcenterVo);
		String orderBy = "order by common.starttime desc";
		String sql = String.format("select %s from %s %s %s limit %d,%d", selectColumn,TenantContext.get().getTenantUuid(),where,orderBy,workcenterVo.getStartNum(),workcenterVo.getPageSize());
		QueryParser parser =ElasticSearchPoolManager.getObjectPool(ProcessTaskEsHandlerBase.POOL_NAME).createQueryParser();
		MultiAttrsQuery query = parser.parse(sql);
		return query.iterate();
	}
	
	/**
	 * 流式分批获取并处理数据
	 */
	public JSONObject getSearchIterate(QueryResultSet resultSet,WorkcenterVo workcenterVo){
		JSONObject returnObj = new JSONObject();
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
		if(resultSet.hasMoreResults()) {
			QueryResult result = resultSet.fetchResult();
			if(!result.getData().isEmpty()) {
				for(MultiAttrsObject el : result.getData()) {
					JSONObject taskJson = new JSONObject();
	            	taskJson.put("taskid", el.getId());
	            	for (Object columnObj: workcenterVo.getResultColumnList()) {
	            		IProcessTaskColumn column = columnComponentMap.get(columnObj);
    					taskJson.put(column.getName(),column.getValueText(el));
	            	}
	            	dataList.add(taskJson);
				}
			}
		}
		returnObj.put("tbodyList", dataList);
		return returnObj;
	}
}
