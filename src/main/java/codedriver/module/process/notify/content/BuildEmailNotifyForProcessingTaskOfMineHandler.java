package codedriver.module.process.notify.content;

import codedriver.framework.notify.core.BuildNotifyContentHandlerBase;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.handler.EmailNotifyHandler;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: BuildEmailNotifyVoForProcessingTaskOfMineHandler
 * @Package: codedriver.module.process.notify.content
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/25 17:23
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class BuildEmailNotifyForProcessingTaskOfMineHandler extends BuildNotifyContentHandlerBase {

    private static Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    protected String myGetPreviewContent(JSONObject config) {
        JSONArray dataColumnList = config.getJSONArray("dataColumnList");
        List<String> columnNameList = new ArrayList<>();
        Map<String, String> collect = columnComponentMap.values()
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
    protected List<NotifyVo> myGetNotifyVoList(NotifyJobVo job) {
        List<NotifyVo> notifyList = new ArrayList<>();

        JSONObject config = job.getConfig();
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
                dataColumns.stream().forEach(o -> columnList.add(columnComponentMap.get(o.toString()).getDisplayName()));
            }else{
                for(Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()){
                    columnList.add(entry.getValue().getDisplayName());
                }
            }

            /** 获取工单查询条件 */
            List<String> stepTeamUuidList = new ArrayList<>();
            JSONObject conditionConfig = config.getJSONObject("conditionConfig");
            if(MapUtils.isNotEmpty(conditionConfig)){
                JSONArray stepTeam = conditionConfig.getJSONArray(ProcessingTaskOfMineHandler.ConditionOptions.STEPTEAM.getValue());
                if (CollectionUtils.isNotEmpty(stepTeam)) {
                    for (Object o : stepTeam) {
                        stepTeamUuidList.add(o.toString().split("#")[1]);
                    }
                }
            }

            /** 查询工单 */
            List<Map<String, Object>> originalTaskList = processTaskService.getTaskListByStepTeamUuidList(stepTeamUuidList);

            /** 按处理人给工单分类 */
            Map<String, List<Map<String, Object>>> userTaskMap = ProcessingTaskOfMineHandler.getUserTaskMap(originalTaskList);

            if(MapUtils.isNotEmpty(userTaskMap)){
                for (Map.Entry<String, List<Map<String, Object>>> entry : userTaskMap.entrySet()) {
                    NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null,null);
                    notifyBuilder.withTitleTemplate(title != null ? title.toString() : null);
                    notifyBuilder.addUserUuid(entry.getKey());

                    /** 绘制工单列表 */
                    StringBuilder taskTable = new StringBuilder();
                    if (StringUtils.isNotBlank(content)) {
                        taskTable.append(content + "</br>");
                    }
                    taskTable.append("<table>");
                    taskTable.append("<tr>");
                    for (String column : columnList) {
                        taskTable.append("<th>" + column + "</th>");
                    }
                    taskTable.append("</tr>");
                    for (Map<String, Object> taskMap : entry.getValue()) {
                        taskTable.append("<tr>");
                        for (String column : columnList) {
                            if (taskMap.containsKey(column)) {
                                taskTable.append("<td>" + (taskMap.get(column) == null ? "" : taskMap.get(column)) + "</td>");
                            }
                        }
                        taskTable.append("</tr>");
                    }
                    taskTable.append("</table>");
                    notifyBuilder.withContentTemplate(taskTable.toString());
                    NotifyVo notifyVo = notifyBuilder.build();
                    notifyList.add(notifyVo);
                }
            }
        }

        return notifyList;
    }

    @Override
    protected String myGetNotifyHandlerClassName() {
        return EmailNotifyHandler.class.getName();
    }

    @Override
    protected String myGetNotifyContentHandlerClassName() {
        return ProcessingTaskOfMineHandler.class.getName();
    }
}
