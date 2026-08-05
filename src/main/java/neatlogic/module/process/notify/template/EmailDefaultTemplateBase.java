package neatlogic.module.process.notify.template;

import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.process.notify.core.IDefaultTemplate;
import org.springframework.stereotype.Component;

import neatlogic.framework.util.$;
public abstract class EmailDefaultTemplateBase implements IDefaultTemplate {


    /**
     * 流程触发点开始
     */
    @Component
    public static class StartProcess extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.startprocess.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.startprocess.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "。\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Urge extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.urge.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.urge.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.urge.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class AbortProcessTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.abortprocesstask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.abortprocesstask.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class RecoverProcessTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.recoverprocesstask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.recoverprocesstask.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CompleteProcessTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.completeprocesstask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.completeprocesstask.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class ReopenProcessTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.reopenprocesstask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.reopenprocesstask.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class ScoreProcessTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.scoreprocesstask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.scoreprocesstask.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.scoreprocesstask.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }
    /** 流程触发点结束 */

    /**
     * 流程步骤触发点开始
     */
    @Component
    public static class Active extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.active.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.active.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.active.getcontent.2") + PROCESSTASK_STEP_NAME + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Assign extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.assign.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.assign.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.assign.getcontent.2") + PROCESSTASK_STEP_WORKER + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class AssignException extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.assignexception.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.assignexception.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.assignexception.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Start extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.start.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.start.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.start.getcontent.2") + PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.start.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Transfer extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.transfer.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.transfer.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.transfer.getcontent.2") + PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.transfer.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Succeed extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.succeed.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.succeed.getcontent.1") + PROCESSTASK_STEP_NAME + "。\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Back extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.back.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.back.getcontent.1") + PROCESSTASK_STEP_NAME + "\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.back.getcontent.2") + REASON + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Retreat extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.retreat.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + $.t("nmpnt.emaildefaulttemplatebase.retreat.getcontent.1") + PROCESSTASK_STEP_NAME + "\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.retreat.getcontent.2") + REASON)
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Hang extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.hang.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.hang.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.hang.getcontent.2"))
                    .append($.t("nmpnt.emaildefaulttemplatebase.hang.getcontent.3") + REASON)//TODO linbq步骤名称有替换成变量
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Pause extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.pause.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.pause.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.pause.getcontent.2"))
                    .append($.t("nmpnt.emaildefaulttemplatebase.pause.getcontent.3") + REASON)
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class Failed extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.failed.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.failed.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.failed.getcontent.2"))
                    .append($.t("nmpnt.emaildefaulttemplatebase.failed.getcontent.3") + REASON)
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }
    /** 流程步骤触发点结束 */

    /**
     * 子流程触发点结束
     */
    /* 任务 触发点开始 */
    @Component
    public static class CreateTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.createtask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.createtask.getcontent.1") + PROCESSTASK_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.createtask.getcontent.2") + TASK_CONFIG_NAME + "：\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.createtask.getcontent.3") + TASK_CONTENT + "\n")
                    .append(TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.createtask.getcontent.4") + TASK_WORKER + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class EditTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.edittask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.edittask.getcontent.1") + TASK_CONFIG_NAME + "：\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.edittask.getcontent.2") + TASK_CONTENT + "\n")
                    .append(TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.edittask.getcontent.3") + TASK_WORKER + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class DeleteTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.deletetask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.deletetask.getcontent.1") + TASK_CONFIG_NAME + "：\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.deletetask.getcontent.2") + TASK_CONTENT + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CompleteTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + $.t("nmpnt.emaildefaulttemplatebase.completetask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(TASK_WORKER + $.t("nmpnt.emaildefaulttemplatebase.completetask.getcontent.1") + TASK_CONFIG_NAME + "\n")
                    .append($.t("nmpnt.emaildefaulttemplatebase.completetask.getcontent.2") + TASK_USER_CONTENT + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CompleteAllTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.completealltask.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.completealltask.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }
    /* 任务 触发点结束 */

    /**
     * SLA触发点开始
     */
    @Component
    public static class Timeout extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.timeout.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.timeout.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.timeout.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }
    /** SLA触发点结束 */

    /**
     * 变更步骤触发点开始
     */
    @Component
    public static class StartChangeStep extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.startchangestep.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.startchangestep.getcontent.1") + CHANGE_STEP_NAME + $.t("nmpnt.emaildefaulttemplatebase.startchangestep.getcontent.2") + CHANGE_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.startchangestep.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CompleteChangeStep extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.completechangestep.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.completechangestep.getcontent.1") + CHANGE_STEP_NAME + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class AbortChangeStep extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.abortchangestep.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.abortchangestep.getcontent.1") + CHANGE_STEP_NAME + "\n")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CommentChangeStep extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.commentchangestep.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + $.t("nmpnt.emaildefaulttemplatebase.commentchangestep.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class CompleteAllChangeStep extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.completeallchangestep.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.completeallchangestep.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class StartChange extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.startchange.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.startchange.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class PauseChange extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.pausechange.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.pausechange.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class RecoverChange extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.recoverchange.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.recoverchange.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }

    @Component
    public static class RestartChange extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.emaildefaulttemplatebase.restartchange.gettitle.1");
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.emaildefaulttemplatebase.restartchange.getcontent.1"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public String getNotifyHandlerType() {
            return NotifyHandlerType.EMAIL.getValue();
        }
    }
    /** 变更步骤触发点结束 */

}
