package codedriver.module.process.notify.content;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.notify.core.BuildNotifyContentHandlerBase;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.handler.MessageNotifyHandler;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
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

    @Override
    protected List<NotifyVo> myGetNotifyVoList(Map<String, Object> map) {
        List<NotifyVo> notifyList = new ArrayList<>();
        String homeUrl = Config.HOME_URL() + TenantContext.get().getTenantUuid() + File.separator;
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

                for (Map<String, Object> taskMap : entry.getValue()) {
                    contentSb.append("查看详情：<a target=\"_blank\" href="
                            + homeUrl + "process.html#/task-detail?processTaskId="
                            + taskMap.get(ProcessWorkcenterField.ID.getValue())
                            + "><b>【工单链接】</b></a></br>");
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
