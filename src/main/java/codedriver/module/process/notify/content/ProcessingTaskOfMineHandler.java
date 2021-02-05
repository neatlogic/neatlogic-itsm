package codedriver.module.process.notify.content;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.notify.core.*;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.handler.EmailNotifyHandler;
import codedriver.framework.notify.handler.MessageNotifyHandler;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Title: 待我处理的工单
 * @Package: codedriver.module.process.notify.content
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/8 11:00
 * <p>
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * <p>
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class ProcessingTaskOfMineHandler extends NotifyContentHandlerBase {

	private static Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;

	@Resource
	private NotifyJobMapper notifyJobMapper;

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
						this.put("validateList", new JSONArray(){
							{
								this.add("required");
							}
						});
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
		messageAttrMap.put(MessageNotifyHandler.class.getName(),new JSONArray(){
			{
				this.add(new JSONObject(){
					{
						this.put("label","标题");
						this.put("name","title");
						this.put("type", "text");
						this.put("validateList", new JSONArray(){
							{
								this.add("required");
							}
						});
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
	 * @Description: 获取工单筛选条件
	 * @Author: laiwt
	 * @Date: 2021/1/8 14:16
	 * @Params: []
	 * @Returns: com.alibaba.fastjson.JSONArray
	**/
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
	 * @Description: 根据通知方式获取消息相关属性
	 * @Author: laiwt
	 * @Date: 2021/1/8 14:17
	 * @Params: [handler]
	 * @Returns: com.alibaba.fastjson.JSONArray
	**/
	@Override
	protected JSONArray getMyMessageAttrList(String handler) {
		return messageAttrMap.get(handler);
	}


	@Override
	protected List<ValueTextVo> getMyDataColumnList() {
		List<ValueTextVo> result = new ArrayList<>();
		Collection<IProcessTaskColumn> values = columnComponentMap.values();
		values.stream().sorted(Comparator.comparing(IProcessTaskColumn::getSort)).forEach(o -> {
			if(!o.getDisabled() && o.getIsShow() && o.getIsExport()){
				result.add(new ValueTextVo(o.getName(),o.getDisplayName()));
			}
		});
		return result;
	}

	@Override
	protected String myPreview(JSONObject config,String notifyHandler) {
		INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
		if(handler == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		IBuildNotifyContentHandler buildNotifyHandler
				= BuildNotifyContentHandlerFactory.getHandler(notifyHandler + "," +ProcessingTaskOfMineHandler.class.getName());
		return buildNotifyHandler.getPreviewContent(config);
	}

	/**
	 * @Description: 获取待发送数据
	 * @Author: laiwt
	 * @Date: 2021/1/8 14:14
	 * @Params: [id] 定时任务ID
	 * @Returns: java.util.List<codedriver.framework.notify.dto.NotifyVo>
	**/
	@Override
	protected List<NotifyVo> getMyNotifyData(Long id) {
	    List<NotifyVo> notifyList = new ArrayList<>();
		NotifyJobVo job = notifyJobMapper.getJobBaseInfoById(id);
		if(job != null){
			INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(job.getNotifyHandler());
			if(notifyHandler == null){
				throw new NotifyHandlerNotFoundException(job.getNotifyHandler());
			}
			IBuildNotifyContentHandler buildNotifyHandler
					= BuildNotifyContentHandlerFactory.getHandler(job.getNotifyHandler() + "," +ProcessingTaskOfMineHandler.class.getName());
			notifyList = buildNotifyHandler.getNotifyVoList(job);
		}
		return notifyList;
	}

	/**
	 * @Description: 按用户将工单分类
	 * @Author: laiwt
	 * @Date: 2021/1/8 14:21
	 * @Params: [originalTaskList]
	 * @Returns: java.util.Map<java.lang.String,java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
	**/
	public static Map<String, List<Map<String, Object>>> getUserTaskMap(List<Map<String, Object>> originalTaskList) {
		Map<String, List<Map<String, Object>>> userTaskMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(originalTaskList)) {
			String currentStepNameWorker = new ProcessTaskCurrentStepWorkerColumn().getName();
			for (Map<String, Object> map : originalTaskList) {
				Object o = map.get(currentStepNameWorker);
				if (o != null) {
					Set<String> array = (HashSet<String>) o;
					for (String uuid : array) {
						List<Map<String, Object>> mapList = new ArrayList<>();
						if (userTaskMap.get(uuid) != null) {
							mapList = userTaskMap.get(uuid);
						}
						if (!mapList.stream().anyMatch(task -> task.get(ProcessWorkcenterField.ID.getValue()).toString().equals(map.get(ProcessWorkcenterField.ID.getValue()).toString()))) {
							mapList.add(map);
						}
						userTaskMap.put(uuid, mapList);
					}
				}
			}
		}
		return userTaskMap;
	}

}
