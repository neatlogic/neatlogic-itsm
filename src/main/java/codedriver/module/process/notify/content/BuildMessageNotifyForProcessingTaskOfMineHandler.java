package codedriver.module.process.notify.content;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.notify.core.BuildNotifyContentHandlerBase;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.handler.MessageNotifyHandler;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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
    protected List<NotifyVo> myGetNotifyVoList(Map<String, Object> map) {
        String taskOverviewUrl = null;
        if(StringUtils.isNotBlank(homeUrl)){
            taskOverviewUrl = homeUrl + TenantContext.get().getTenantUuid() + File.separator + "process.html#/task-overview";
        }
        List<NotifyVo> notifyList = new ArrayList<>();
        Object title = map.get("title");
        Object content = map.get("content");
        Object userTaskMapObj = map.get("userTaskMap");
        if (userTaskMapObj != null) {
            Map<String, List<Map<String, Object>>> userTaskMap = (Map<String, List<Map<String, Object>>>) userTaskMapObj;
            for (Map.Entry<String, List<Map<String, Object>>> entry : userTaskMap.entrySet()) {
                NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null, ProcessTaskMessageHandler.class);
                notifyBuilder.withTitleTemplate(title != null ? title.toString() : null);
                notifyBuilder.addUserUuid(entry.getKey());

                StringBuilder contentSb = new StringBuilder();
                if (content != null) {
                    contentSb.append(content.toString() + "</br>");
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
