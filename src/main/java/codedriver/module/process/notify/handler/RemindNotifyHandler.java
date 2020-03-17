package codedriver.module.process.notify.handler;

import codedriver.framework.process.notify.core.NotifyHandlerBase;
import codedriver.framework.reminder.core.GlobalReminderFactory;
import codedriver.framework.reminder.core.IGlobalReminder;
import codedriver.framework.reminder.dto.ReminderMessageVo;
import codedriver.module.process.notify.dto.NotifyVo;
import codedriver.module.process.reminder.handler.ProcessTaskRemindHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @program: codedriver
 * @description: 消息中心对接
 * @create: 2020-03-16 14:25
 **/
@Component
public class RemindNotifyHandler extends NotifyHandlerBase {

    private static Logger logger = LoggerFactory.getLogger(RemindNotifyHandler.class);

    @Override
    protected void myExecute(NotifyVo notifyVo) {
        this.sendRemind(notifyVo);
    }

    private void sendRemind(NotifyVo notifyVo){
        if (notifyVo.getToUserList().size() > 0){
            ReminderMessageVo message = new ReminderMessageVo();
            message.setTitle(notifyVo.getTitle());
            message.setContent(notifyVo.getContent());
            message.setFromUser(notifyVo.getFromUser());
            message.setReceiverList(notifyVo.getToUserIdList());
            IGlobalReminder reminder = GlobalReminderFactory.getReminder(ProcessTaskRemindHandler.class.getName());
            reminder.send(message);
        }
    }

    @Override
    public String getName() {
        return "消息中心插件";
    }
}
