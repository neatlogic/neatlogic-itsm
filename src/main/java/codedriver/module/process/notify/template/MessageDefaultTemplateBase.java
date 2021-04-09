package codedriver.module.process.notify.template;

import codedriver.framework.notify.core.NotifyHandlerType;
import codedriver.framework.process.notify.core.IDefaultTemplate;

public abstract class MessageDefaultTemplateBase implements IDefaultTemplate {

    @Override
    public String getNotifyHandlerType() {
        return NotifyHandlerType.MESSAGE.getValue();
    }

    public static class Active extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤激活提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "已流转至【${DATA.step.name}】；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知步骤激活默认模板";
        }

    }

    public static class Start extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "处理人响应提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "的【${DATA.step.name}】步骤已由处理人【${DATA.step.majorUser.userName}】受理；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知步骤激活默认模板";
        }

    }

    public static class Transfer extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤转交提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "中，【${DATA.currentUserName}】已将【${DATA.step.name}】步骤转交给" + PROCESSTASK_STEP_WORKER + "处理；<br>")
                    .append("原因：【${DATA.content}】 ；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知步骤激活默认模板";
        }

    }

    public static class Urge extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "您收到了一条催办提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "中，【${DATA.currentUserName}】发起了一条催办通知，请尽快处理；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知催办默认模板";
        }
    }

    public static class Succeed extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤完成提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "的【${DATA.step.name}】步骤已处理完成；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知成功默认模板";
        }
    }

    public static class Back extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤退回提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "已回退至【${DATA.step.name}】；<br>")
                    .append("原因：【${DATA.content}】 ；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知退回默认模板";
        }
    }

    public static class Retreat extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤撤回提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.currentUserName}】已将工单" + PROCESSTASK_SERIALNUMBER_TITLE + "的【${DATA.step.name}】步骤撤回；<br>")
                    .append("原因：【${DATA.content}】 ；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知撤回默认模板";
        }
    }

    public static class Hang extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤挂起提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "中，【${DATA.step.name}】步骤已挂起；<br>")
                    .append("原因：因【步骤名称】步骤回退导致  ；<br>")//TODO linbq步骤名称有替换成变量
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知挂起默认模板";
        }
    }

    public static class Abort extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "取消工单——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.currentUser}】取消了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知取消默认模板";
        }
    }

    public static class Recover extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "恢复工单——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.currentUser}】恢复了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知恢复默认模板";
        }
    }

    public static class Failed extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "步骤失败提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "中，【${DATA.step.name}】步骤流转失败；<br>")
                    .append("原因：【${DATA.step.error}】  ；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知失败默认模板";
        }
    }

    public static class CreateSubtask extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "子任务创建提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "中，【${DATA.subtask.ownerName}】为【${DATA.step.name}】步骤创建了一个子任务，待【${DATA.subtask.userName}】处理；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知子任务创建默认模板";
        }
    }

    public static class EditSubtask extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "更新子任务提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.subtask.ownerName}】更新了分配给【${DATA.subtask.userName}】的任务：<br>")
                    .append("【${DATA.subtask.content}】；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知更新子任务默认模板";
        }
    }

    public static class CompleteSubtask extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "子任务完成提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.subtask.userName}】完成了【${DATA.subtask.ownerName}】分配的任务：<br>")
                    .append("【${DATA.subtask.content}】；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知子任务完成默认模板";
        }
    }

    public static class RedoSubtask extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "退回子任务提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.subtask.ownerName}】退回了分配给【${DATA.subtask.userName}】的任务：<br>")
                    .append("【${DATA.subtask.content}】；<br>")
                    .append("原因：【${DATA.content}】  ；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知退回子任务默认模板";
        }
    }

    public static class AbortSubtask extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return "取消子任务提醒——" + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.subtask.ownerName}】取消了分配给【${DATA.subtask.userName}】的任务：<br>")
                    .append("【${DATA.subtask.content}】；<br>")
                    .append(PROCESSTASK_DETAILS_LINK)
                    .toString();
        }

        @Override
        public String description() {
            return "消息通知取消子任务默认模板";
        }
    }
}
