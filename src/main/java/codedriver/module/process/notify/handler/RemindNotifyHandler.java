package codedriver.module.process.notify.handler;

import codedriver.framework.notify.core.NotifyHandlerBase;
import codedriver.framework.notify.core.NotifyHandlerType;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.core.IGlobalReminderHandler;
import codedriver.framework.reminder.dto.ReminderMessageVo;
import codedriver.module.process.reminder.handler.ProcessTaskRemindHandler;

/**
 * @program: codedriver
 * @description: 消息中心对接
 * @create: 2020-03-16 14:25
 **/
//@Component
public class RemindNotifyHandler extends NotifyHandlerBase {

//    private static Logger logger = LoggerFactory.getLogger(RemindNotifyHandler.class);

    @Override
    protected void myExecute(NotifyVo notifyVo) {
//        this.sendRemind(notifyVo);
    }

//    private void sendRemind(NotifyVo notifyVo){
//        if (notifyVo.getToUserList().size() > 0){
//            ReminderMessageVo message = new ReminderMessageVo();
//            message.setTitle(notifyVo.getTitle());
//            message.setContent(notifyVo.getContent());
//            message.setFromUser(notifyVo.getFromUser());
//            message.setReceiverList(notifyVo.getToUserUuidList());
//            IGlobalReminderHandler reminder = GlobalReminderHandlerFactory.getReminder(ProcessTaskRemindHandler.class.getName());
//            reminder.send(message);
//        }
//    }

    @Override
    public String getName() {
        return NotifyHandlerType.REMIND.getText();
    }

	@Override
	public String getType() {
		return NotifyHandlerType.REMIND.getValue();
	}
}
