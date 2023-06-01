package neatlogic.module.process.message.handler;

import neatlogic.framework.message.core.MessageHandlerBase;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dto.NotifyVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.util.I18nUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: ProcessTaskMessageHandler
 * @Package neatlogic.framework.process.message.handler
 * @Description: 工单消息处理器
 * @Author: linbq
 * @Date: 2020/12/31 14:28
 **/
@Service
public class ProcessTaskMessageHandler extends MessageHandlerBase {

    @Override
    public String getName() {
        return "IT服务";
    }

    @Override
    public String getDescription() {
        return "实时显示待处理工单信息，支持快速审批";
    }

    @Override
    public boolean getNeedCompression() {
        return false;
    }

    @Override
    public NotifyVo compress(List<NotifyVo> notifyVoList) {
        return null;
    }

    /**
     * 获取发起方信息，目前用于异常邮件
     *
     * @return 发起方信息
     */
    @Override
    public String getCallerMessage(NotifyVo notifyVo) {
        ProcessTaskStepVo taskStepVo = (ProcessTaskStepVo) notifyVo.getCallerData();
        String policyHandlerName = StringUtils.EMPTY;
        INotifyPolicyHandler policyHandler = NotifyPolicyHandlerFactory.getHandler(notifyVo.getNotifyPolicyHandler());
        if (policyHandler != null) {
            policyHandlerName = policyHandler.getName();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p>");
        stringBuilder.append(I18nUtils.getMessage("IT服务"));
        stringBuilder.append("-");
        stringBuilder.append(I18nUtils.getMessage(policyHandlerName));
        stringBuilder.append("-");
        stringBuilder.append(notifyVo.getCallerNotifyPolicyVo().getName());
        stringBuilder.append("-");
        stringBuilder.append(notifyVo.getTriggerType().getText());
        stringBuilder.append("</p>");
        String processTaskIdStr = "";
        String stepName = "";
        String stepIdStr = "";
        if (taskStepVo != null) {
            Long processTaskId = taskStepVo.getProcessTaskId();
            if (processTaskId != null) {
                processTaskIdStr = processTaskId.toString();
            }
            Long stepId = taskStepVo.getId();
            if (stepId != null) {
                stepIdStr = stepId.toString();
            }
            if (StringUtils.isNotBlank(taskStepVo.getName())) {
                stepName = taskStepVo.getName();
            }
        }
        stringBuilder.append(I18nUtils.getMessage("工单号：{0}、步骤名：{1}({2})", processTaskIdStr, stepName, stepIdStr));
        return stringBuilder.toString();
    }
}
