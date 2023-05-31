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
        return "modulegroup.itsm.a";
    }

    @Override
    public String getDescription() {
        return "handler.message.itsm.description";
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
        stringBuilder.append(I18nUtils.getMessage("modulegroup.itsm.a"));
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
        stringBuilder.append(I18nUtils.getMessage("handler.message.itsm.callermessage", processTaskIdStr, stepName, stepIdStr));
        return stringBuilder.toString();
    }
}
