package codedriver.module.process.notify.content;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
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
import codedriver.framework.process.notify.constvalue.TaskNotifyTriggerType;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

	@Resource
	private NotifyJobMapper notifyJobMapper;

    @Resource
    protected ProcessTaskService processTaskService;

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

	/** 需要可选择工单显示字段的通知方式
	 * 目前只有邮件通知需要，因为邮件内容为工单列表
	 */
	private static List<String> notifyHandlersWhichCanChooseTaskColumn = new ArrayList<>();

	private static Map<String,BuildNotifyHandler> handlerMap = new HashMap<>();

	@PostConstruct
	public void init() {

		/**
		 * 不同的通知内容与通知方式插件对应不同的属性
		 * 例如此插件下的邮件通知有邮件标题、邮件正文、接收人
		 * 将来扩展通知方式插件时，可在messageAttrMap中put对应的插件类与属性
		 */
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

		notifyHandlersWhichCanChooseTaskColumn.add(EmailNotifyHandler.class.getName());

		/**
		 * 不同的通知方式返回内容形式不同的NotifyVo
		 * 例如邮件通知的形式为工单列表
		**/
		handlerMap.put(EmailNotifyHandler.class.getName(),new BuildNotifyHandler(){
			@Override
			public String getPreviewContent(JSONObject config) {
				JSONArray dataColumnList = config.getJSONArray("dataColumnList");
				List<String> columnNameList = new ArrayList<>();
				Map<String, String> collect = ProcessTaskColumnFactory.columnComponentMap.values()
						.stream().collect(Collectors.toMap(e -> e.getName(), e -> e.getDisplayName()));
				for(Object column : dataColumnList){
					columnNameList.add(collect.get(column.toString()));
				}
				List<Map<String, String>> dataList = new ArrayList<>();
				for(int i = 0;i < 12;i++){
					Map<String, String> map = new HashMap<>();
					map.put("标题","机房进出申请-202101080000" + i);
					map.put("工单号","202101080000" + i);
					map.put("上报人","admin");
					map.put("优先级","P3");
					map.put("代报人","admin");
					map.put("当前步骤处理人","张三");
					map.put("当前步骤名","机房监督");
					map.put("工单状态","处理中");
					map.put("服务目录","机房");
					map.put("服务类型","事件");
					map.put("服务","机房进出申请");
					map.put("上报时间","2021-01-08 10:10:57");
					map.put("时间窗口","工作日");
					map.put("结束时间","2021-01-12 15:18:23");
					map.put("剩余时间","距离超时：3天");
					dataList.add(map);
				}

				StringBuilder taskTable = new StringBuilder();
				taskTable.append("<div class=\"ivu-card-body tstable-container\">");
				taskTable.append("<table class=\"tstable-body\">");
				taskTable.append("<thead>");
				taskTable.append("<tr class=\"th-left\">");
				for(String column : columnNameList){
					taskTable.append("<th>" + column + "</th>");
				}
				taskTable.append("</tr>");
				taskTable.append("</thead>");
				taskTable.append("<tbody>");
				for(Map<String, String> map : dataList){
					taskTable.append("<tr>");
					for(String column : columnNameList){
						taskTable.append("<td>" + map.get(column) + "</td>");
					}
					taskTable.append("</tr>");
				}
				taskTable.append("</tbody>");
				taskTable.append("</table>");
				taskTable.append("</div>");
				return taskTable.toString();
			}

			@Override
			public List<NotifyVo> getNotifyVoList(JSONObject config) {
				List<NotifyVo> notifyList = new ArrayList<>();

				if(MapUtils.isNotEmpty(config)){
					String title = null;
					String content = null;
					JSONObject messageConfig = config.getJSONObject("messageConfig");
					if(MapUtils.isNotEmpty(messageConfig)){
						title = messageConfig.getString("title");
						content = messageConfig.getString("content");
					}

					/** 需要的工单字段 */
					JSONArray dataColumns = config.getJSONArray("dataColumnList");
					List<String> columnList = new ArrayList<>();
					if(CollectionUtils.isNotEmpty(dataColumns)){
						dataColumns.stream().forEach(o -> columnList.add(ProcessTaskColumnFactory.columnComponentMap.get(o.toString()).getDisplayName()));
					}else{
						for(Map.Entry<String, IProcessTaskColumn> entry : ProcessTaskColumnFactory.columnComponentMap.entrySet()){
							columnList.add(entry.getValue().getDisplayName());
						}
					}
					/** 获取按用户分好类的工单列表 **/
					Map<String, List<Map<String, Object>>> userTaskMap = getUserTaskMap(config);

					drawTaskTable(notifyList, title, content, columnList, userTaskMap);
				}

				return notifyList;
			}

			private void drawTaskTable(List<NotifyVo> notifyList, String title, String content, List<String> columnList, Map<String, List<Map<String, Object>>> userTaskMap) {
				if(MapUtils.isNotEmpty(userTaskMap)){
					for (Map.Entry<String, List<Map<String, Object>>> entry : userTaskMap.entrySet()) {
						NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null,null);
						notifyBuilder.withTitleTemplate(title != null ? title : null);
						notifyBuilder.addUserUuid(entry.getKey());

						/** 绘制工单列表 */
						StringBuilder taskTable = new StringBuilder();
						if (StringUtils.isNotBlank(content)) {
							taskTable.append(content + "</br>");
						}
						taskTable.append("<!DOCTYPE html>\n" +
								"<html>\n" +
								"<head>\n" +
								"<meta charset=\"utf-8\">\n" +
								"<style type=\"text/css\">\n" +
								"html {\n" +
								"    font-family: sans-serif;\n" +
								"    -ms-text-size-adjust: 100%;\n" +
								"    -webkit-text-size-adjust: 100%;\n" +
								"}\n" +
								"body {\n" +
								"    margin: 10px;\n" +
								"}\n" +
								"table {\n" +
								"    border-collapse: collapse;\n" +
								"    border-spacing: 0;\n" +
								"}\n" +
								"td,th {\n" +
								"    padding: 0;\n" +
								"}\n" +
								".pure-table {\n" +
								"    border-collapse: collapse;\n" +
								"    border-spacing: 0;\n" +
								"    empty-cells: show;\n" +
								"    border: 1px solid #cbcbcb;\n" +
								"} \n" +
								".pure-table caption {\n" +
								"    color: #000;\n" +
								"    font: italic 85%/1 arial,sans-serif;\n" +
								"    padding: 1em 0;\n" +
								"    text-align: center;\n" +
								"}\n" +
								".pure-table td,.pure-table th {\n" +
								"    border-left: 1px solid #cbcbcb;\n" +
								"    border-width: 0 0 0 1px;\n" +
								"    font-size: inherit;\n" +
								"    margin: 0;\n" +
								"    overflow: visible;\n" +
								"    padding: .5em 1em;\n" +
								"}\n" +
								".pure-table thead {\n" +
								"    background-color: #e0e0e0;\n" +
								"    color: #000;\n" +
								"    text-align: left;\n" +
								"    vertical-align: center;\n" +
								"}\n" +
								".pure-table td {\n" +
								"    background-color: transparent;\n" +
								"}\n" +
								".pure-table-bordered td {\n" +
								"    border-bottom: 1px solid #cbcbcb;\n" +
								"} \n" +
								".pure-table-bordered tbody>tr:last-child>td {\n" +
								"    border-bottom-width: 0;\n" +
								"}\n" +
								"</style>\n" +
								"</head>\n" +
								"<body>");
						taskTable.append("<table class=\"pure-table pure-table-bordered\">");
						taskTable.append("<thead>");
						taskTable.append("<tr>");
						for (String column : columnList) {
							taskTable.append("<th>" + column + "</th>");
						}
						taskTable.append("</tr>");
						taskTable.append("</thead>");
						taskTable.append("<tbody>");
						for (Map<String, Object> taskMap : entry.getValue()) {
							taskTable.append("<tr>");
							for (String column : columnList) {
								if (taskMap.containsKey(column)) {
									taskTable.append("<td>" + (taskMap.get(column) == null ? "" : taskMap.get(column)) + "</td>");
								}
							}
							taskTable.append("</tr>");
						}
						taskTable.append("</tbody>");
						taskTable.append("</table>");
						taskTable.append("</html>");
						notifyBuilder.withContentTemplate(taskTable.toString());
						NotifyVo notifyVo = notifyBuilder.build();
						notifyList.add(notifyVo);
					}
				}
			}
		});
		handlerMap.put(MessageNotifyHandler.class.getName(),new BuildNotifyHandler(){
			private final String homeUrl = Config.HOME_URL();

			@Override
			public String getPreviewContent(JSONObject config) {
				if(StringUtils.isNotBlank(homeUrl)){
					String taskOverviewUrl = homeUrl + TenantContext.get().getTenantUuid() + File.separator + "process.html#/task-overview";
					return "您有 <span style=\"color:red\">"
							+ "5</span> 条待处理工单，请前往<a href=\"" + taskOverviewUrl
							+ "\" target=\"_blank\">【工单中心】</a>，点击【我的待办】按钮查看";
				}else{
					return "您有 <span style=\"color:red\">5</span> 条待处理工单，" +
							"请前往【IT服务->工单中心->所有】，点击【我的待办】按钮查看";
				}
			}

			@Override
			public List<NotifyVo> getNotifyVoList(JSONObject config) {
				String taskOverviewUrl = null;
				if(StringUtils.isNotBlank(homeUrl)){
					taskOverviewUrl = homeUrl + TenantContext.get().getTenantUuid() + File.separator + "process.html#/task-overview";
				}
				List<NotifyVo> notifyList = new ArrayList<>();

				if(MapUtils.isNotEmpty(config)){
					String title = null;
					String content = null;
					JSONObject messageConfig = config.getJSONObject("messageConfig");
					if(MapUtils.isNotEmpty(messageConfig)){
						title = messageConfig.getString("title");
						content = messageConfig.getString("content");
					}

					/** 获取按用户分好类的工单列表 */
					Map<String, List<Map<String, Object>>> userTaskMap = getUserTaskMap(config);

					if(MapUtils.isNotEmpty(userTaskMap)){
						for (Map.Entry<String, List<Map<String, Object>>> entry : userTaskMap.entrySet()) {
							NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(TaskNotifyTriggerType.URGE, ProcessTaskMessageHandler.class);
							notifyBuilder.withTitleTemplate(title != null ? title : null);
							notifyBuilder.addUserUuid(entry.getKey());

							StringBuilder contentSb = new StringBuilder();
							if (StringUtils.isNotBlank(content)) {
								contentSb.append(content + "</br>");
							}
							if(StringUtils.isNotBlank(taskOverviewUrl)){
								contentSb.append("您有 <span style=\"color:red\">"
										+ entry.getValue().size() + "</span> 条待处理工单，请前往<a href=\""
										+ taskOverviewUrl + "\" target=\"_blank\">【工单中心】</a>，点击【我的待办】按钮查看");
							}else{
								contentSb.append("您有 <span style=\"color:red\">"
										+ entry.getValue().size()
										+ "</span> 条待处理工单，请前往【IT服务->工单中心->所有】，点击【我的待办】按钮查看");
							}

							notifyBuilder.withContentTemplate(contentSb.toString());
							NotifyVo notifyVo = notifyBuilder.build();
							notifyList.add(notifyVo);
						}
					}
				}
				return notifyList;
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
	protected List<ValueTextVo> getMyDataColumnList(String notifyHandler) {
		List<ValueTextVo> result = new ArrayList<>();
		INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
		if(handler == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		if(notifyHandlersWhichCanChooseTaskColumn.contains(notifyHandler)){
			Collection<IProcessTaskColumn> values = ProcessTaskColumnFactory.columnComponentMap.values();
			values.stream().sorted(Comparator.comparing(IProcessTaskColumn::getSort)).forEach(o -> {
				if(!o.getDisabled() && o.getIsShow() && o.getIsExport()){
					result.add(new ValueTextVo(o.getName(),o.getDisplayName()));
				}
			});
		}
		return result;
	}

	@Override
	protected String myPreview(JSONObject config,String notifyHandler) {
		INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
		if(handler == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		BuildNotifyHandler buildHandler = handlerMap.get(notifyHandler);
		return buildHandler.getPreviewContent(config);
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
			BuildNotifyHandler buildHandler = handlerMap.get(job.getNotifyHandler());
			notifyList = buildHandler.getNotifyVoList(job.getConfig());
		}
		return notifyList;
	}

	/**
	 * @Description: 查询工单，并按用户分类
	 * @Author: laiwt
	 * @Date: 2021/3/4 15:15
	 * @Params: [config]
	 * @Returns: java.util.Map<java.lang.String,java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
	**/
	private Map<String, List<Map<String, Object>>> getUserTaskMap(JSONObject config) {
		/** 获取工单查询条件 */
		List<String> stepTeamUuidList = new ArrayList<>();
		JSONObject conditionConfig = config.getJSONObject("conditionConfig");
		if (MapUtils.isNotEmpty(conditionConfig)) {
			JSONArray stepTeam = conditionConfig.getJSONArray(ConditionOptions.STEPTEAM.getValue());
			if (CollectionUtils.isNotEmpty(stepTeam)) {
				for (Object o : stepTeam) {
					stepTeamUuidList.add(o.toString().split("#")[1]);
				}
			}
		}

		/** 查询工单 */
		List<Map<String, Object>> originalTaskList = processTaskService.getTaskListByStepTeamUuidList(stepTeamUuidList);

		/** 按处理人给工单分类 */
		return classifyTaskByUser(originalTaskList);
	}

	/**
	 * @Description: 按用户将工单分类
	 * @Author: laiwt
	 * @Date: 2021/1/8 14:21
	 * @Params: [originalTaskList]
	 * @Returns: java.util.Map<java.lang.String,java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
	**/
	private Map<String, List<Map<String, Object>>> classifyTaskByUser(List<Map<String, Object>> originalTaskList) {
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
