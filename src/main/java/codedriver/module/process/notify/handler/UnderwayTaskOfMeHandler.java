package codedriver.module.process.notify.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyContentHandlerBase;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.handler.EmailNotifyHandler;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.service.WorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 待我处理的工单
 */
@Component
public class UnderwayTaskOfMeHandler extends NotifyContentHandlerBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	WorkcenterService workcenterService;

	public enum ConditionOptions{
		STEPTEAM("stepteam","处理组",Expression.INCLUDE.getExpression());

		private String value;
		private String text;
		private String expression;
		private ConditionOptions(String value, String text, String expression) {
			this.value = value;
			this.text = text;
			this.expression = expression;
		}
		public String getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		public String getExpression() {
			return expression;
		}

		public static ConditionOptions getConditionOption(String _value) {
			for(ConditionOptions e : values()) {
				if(e.value.equals(_value)) {
					return e;
				}
			}
			return null;
		}
	}

	private static Map<String, JSONArray> messageAttrMap = new HashMap<>();

	/**
	 * 不同的通知内容与通知方式插件对应不同的属性
	 * 例如此插件下的邮件通知有邮件标题、邮件正文、接收人
	 * 将来扩展通知方式插件时，可在messageAttrMap中put对应的插件类与属性
	 */
	static {
		messageAttrMap.put(EmailNotifyHandler.class.getName(),new JSONArray(){
			{
				this.add(new JSONObject(){
					{
						this.put("label","标题");
						this.put("name","title");
						this.put("type", "text");
						this.put("validateList", "['required']");
					}
				});
				this.add(new JSONObject(){
					{
						this.put("label","内容");
						this.put("name","content");
						this.put("type", FormHandlerType.TEXTAREA.toString());
					}
				});
				this.add(new JSONObject(){
					{
						this.put("label","接收人");
						this.put("name","toList");
						this.put("type", FormHandlerType.SELECT.toString());
						this.put("placeholder","工单内容对应的处理人");
						this.put("disabled",true);
					}
				});
			}
		});
	}

	@Override
	public String getName() {
		return "待我处理的工单";
	}

	@Override
	public String getType() {
		return Type.DYNAMIC.getValue();
	}

	/**
	 * 获取工单筛选条件
	 * @return
	 */
	@Override
	protected JSONArray getMyConditionOptionList() {
		JSONArray params = new JSONArray();
		for(ConditionOptions option : ConditionOptions.values()) {
			IConditionHandler condition = ConditionHandlerFactory.getHandler(option.getValue());
			if(condition != null) {
				JSONObject obj = condition.getConfig();
				obj.put("type",condition.getHandler(ProcessConditionModel.SIMPLE.getValue()));
				obj.put("name",condition.getName());
				obj.put("label",condition.getDisplayName());
				/** 不同的条件有其特殊的表单属性，根据需要自行添加 */
				if(ConditionOptions.STEPTEAM.getValue().equals(option.getValue())){
					obj.put("groupList",new JSONArray(){
						{
							this.add(GroupSearch.TEAM.getValue());
						}
					});
					obj.put("multiple", true);
					obj.put("search", true);
				}
				params.add(obj);
			}
		}
		return params;
	}

	/**
	 * 根据通知方式插件获取发件相关属性
	 * @param handler
	 * @return
	 */
	@Override
	protected JSONArray getMyMessageAttrList(String handler) {
		return messageAttrMap.get(handler);
	}

	/**
	 * 获取工单列表数据列
	 * @return
	 */
	@Override
	protected List<ValueTextVo> getMyDataColumnList() {
		List<ValueTextVo> result = new ArrayList<>();
		Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
		Collection<IProcessTaskColumn> values = columnComponentMap.values();
		values.stream().forEach(o -> {
			if(!o.getDisabled() && o.getIsShow()){
				result.add(new ValueTextVo(o.getName(),o.getDisplayName()));
			}
		});
		return result;
	}

	/**
	 * 获取待发送数据
	 * @param id
	 */
	@Override
	protected List<NotifyVo> getMyNotifyData(Long id) {
		NotifyJobVo job = notifyJobMapper.getJobBaseInfoById(id);
		INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(job.getNotifyHandler());
		if(notifyHandler == null){
			throw new NotifyHandlerNotFoundException(job.getNotifyHandler());
		}
		// TODO 根据通知方式和配置获取工单数据并组装成消息内容，通知方式不一样消息内容也不一样，最后要按接收人分类
		String config = job.getConfig();

		JSONArray dataColumnList = (JSONArray)JSONPath.read(config, "dataColumnList");

		/** 组装工单查询参数 */
		JSONArray conditionGroupList = getConditionList(config);
		JSONObject conditionObj = new JSONObject();
		conditionObj.put("conditionGroupList",conditionGroupList);
		WorkcenterVo workcenterVo = new WorkcenterVo(conditionObj);
		QueryResultSet resultSet = workcenterService.searchTaskIterate(workcenterVo);

		Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
		List<WorkcenterTheadVo> theadList = workcenterService.getWorkcenterTheadList(workcenterVo, columnComponentMap,null);
		theadList = theadList.stream()
				.filter(o -> o.getDisabled() == 0 && o.getIsExport() == 1 && o.getIsShow() == 1)
				.sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
		List<Map<String,Object>> list = new ArrayList<>();

		while(resultSet.hasMoreResults()){
			QueryResult result = resultSet.fetchResult();
			if(!result.getData().isEmpty()){
				for(MultiAttrsObject el : result.getData()){
					Map<String,Object> map = new LinkedHashMap<>();
					if(CollectionUtils.isNotEmpty(dataColumnList)){
						for(Object columnName : dataColumnList){
							IProcessTaskColumn column = columnComponentMap.get(columnName.toString());
							Object value = column.getSimpleValue(column.getValue(el));
							map.put(column.getDisplayName(),value);
						}
					}else{
						for(WorkcenterTheadVo vo : theadList){
							IProcessTaskColumn column = columnComponentMap.get(vo.getName());
							Object value = column.getSimpleValue(column.getValue(el));
							map.put(column.getDisplayName(),value);
						}
					}
					list.add(map);
				}
			}
		}
		return null;
	}

	private JSONArray getConditionList(String config) {
		JSONArray conditionGroupList = new JSONArray();
		if(StringUtils.isNotBlank(config)){
			JSONObject obj = new JSONObject();
			JSONArray conditionList = (JSONArray) JSONPath.read(config, "conditionList");
			/** 为每个condition补上uuid、type、expression */
			for(int i = 0;i < conditionList.size();i++){
				JSONObject condition = conditionList.getJSONObject(i);
				condition.put("uuid", UUID.randomUUID().toString().replace("-", ""));
				condition.put("type", ProcessFieldType.COMMON.getValue());
				for(ConditionOptions conditionOptions : ConditionOptions.values()){
					if(conditionOptions.getValue().equals(condition.getString("name"))){
						condition.put("expression",conditionOptions.getExpression());
						break;
					}
				}
			}
			/** 补上步骤状态为待处理的condition */
			JSONObject stepStatus = new JSONObject();
			stepStatus.put("uuid",UUID.randomUUID().toString().replace("-", ""));
			stepStatus.put("name", ProcessWorkcenterField.STEP_STATUS.getValue());
			stepStatus.put("type",ProcessFieldType.COMMON.getValue());
			stepStatus.put("expression", Expression.INCLUDE.getExpression());
			stepStatus.put("valueList", new JSONArray(){
				{
					this.add(ProcessTaskStatus.PENDING.getValue());
				}
			});
			conditionList.add(stepStatus);
			obj.put("conditionList",conditionList);
			/** 构造conditionRelList */
			if(conditionList.size() > 1){
				JSONArray conditionRelList = new JSONArray();
				for(int i = 0;i < conditionList.size() - 1;i++){
					JSONObject rel = new JSONObject();
					rel.put("from",conditionList.getJSONObject(i).getString("uuid"));
					rel.put("to",conditionList.getJSONObject(i + 1).getString("uuid"));
					rel.put("joinType","and");
					conditionRelList.add(rel);
				}
				obj.put("conditionRelList",conditionRelList);
			}
			conditionGroupList.add(obj);
		}
		return conditionGroupList;
	}
}
