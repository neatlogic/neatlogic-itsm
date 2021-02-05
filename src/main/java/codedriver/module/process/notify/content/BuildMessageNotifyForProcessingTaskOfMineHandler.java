package codedriver.module.process.notify.content;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.notify.core.BuildNotifyContentHandlerBase;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.handler.MessageNotifyHandler;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Title: BuildMessageNotifyVoForProcessingTaskOfMineHandler
 * @Package: codedriver.module.process.notify.content
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/25 17:23
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class BuildMessageNotifyForProcessingTaskOfMineHandler extends BuildNotifyContentHandlerBase {

    private static final String homeUrl = Config.HOME_URL();

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    protected String myGetPreviewContent(JSONObject config) {
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
    protected List<NotifyVo> myGetNotifyVoList(NotifyJobVo job) {
        String taskOverviewUrl = null;
        if(StringUtils.isNotBlank(homeUrl)){
            taskOverviewUrl = homeUrl + TenantContext.get().getTenantUuid() + File.separator + "process.html#/task-overview";
        }
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
                    NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null, ProcessTaskMessageHandler.class);
                    notifyBuilder.withTitleTemplate(title != null ? title.toString() : null);
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

    @Override
    protected String myGetNotifyHandlerClassName() {
        return MessageNotifyHandler.class.getName();
    }

    @Override
    protected String myGetNotifyContentHandlerClassName() {
        return ProcessingTaskOfMineHandler.class.getName();
    }
}
