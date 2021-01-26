package codedriver.module.process.notify.content;

import codedriver.framework.notify.core.BuildNotifyVoHandlerBase;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.handler.EmailNotifyHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class BuildEmailNotifyVoForProcessingTaskOfMineHandler extends BuildNotifyVoHandlerBase {

    @Override
    protected List<NotifyVo> myGetNotifyVoList(Map<String, Object> map) {
        List<NotifyVo> notifyList = new ArrayList<>();
        Object title = map.get("title");
        Object content = map.get("content");
        Object columnListObj = map.get("columnList");
        Object userTaskMapObj = map.get("userTaskMap");
        if (userTaskMapObj != null) {
            Map<String, List<Map<String, Object>>> userTaskMap = (Map<String, List<Map<String, Object>>>) userTaskMapObj;
            List<String> columnList = (List<String>) columnListObj;
            for (Map.Entry<String, List<Map<String, Object>>> entry : userTaskMap.entrySet()) {
                NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null,null);
                notifyBuilder.withTitleTemplate(title != null ? title.toString() : null);
                notifyBuilder.addUserUuid(entry.getKey());

                /** 绘制工单列表 */
                StringBuilder taskTable = new StringBuilder();
                if (content != null) {
                    taskTable.append(content.toString() + "</br>");
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
