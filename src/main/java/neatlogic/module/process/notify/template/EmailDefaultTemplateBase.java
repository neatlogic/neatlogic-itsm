package neatlogic.module.process.notify.template;

import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.process.notify.core.IDefaultTemplate;
import org.springframework.stereotype.Component;

public abstract class EmailDefaultTemplateBase implements IDefaultTemplate {


    /**
     * 流程触发点开始
     */
    @Component
    public static class StartProcess extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "工单上报完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.ownername}】上报工单" + PROCESSTASK_SERIALNUMBER_TITLE + "。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "催办工单提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "对工单" + PROCESSTASK_SERIALNUMBER_TITLE + "发起催办。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "取消工单提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "取消了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "恢复工单提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "恢复了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "工单完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "完成了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "重新打开提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "重新打开了工单" + PROCESSTASK_SERIALNUMBER_TITLE + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "工单评分提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "对工单" + PROCESSTASK_SERIALNUMBER_TITLE + "进行了评分。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤激活提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "已流转至" + PROCESSTASK_STEP_NAME + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤分配处理人提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("系统已为工单步骤" + PROCESSTASK_STEP_NAME + "分配处理人" + PROCESSTASK_STEP_WORKER + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤处理人分配异常提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单步骤" + PROCESSTASK_STEP_NAME + "分配处理人异常，原因：根据分配策略没有找到处理人")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "处理人响应提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单步骤" + PROCESSTASK_STEP_NAME + "已由" + PROCESSTASK_STEP_WORKER + "受理。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤转交提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "已将" + PROCESSTASK_STEP_NAME + "转交给" + PROCESSTASK_STEP_WORKER + "处理。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "完成步骤" + PROCESSTASK_STEP_NAME + "。\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤回退提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "回退步骤至" + PROCESSTASK_STEP_NAME + "\n")
                    .append(",原因：" + REASON + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤撤回提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(OPERATOR + "撤回步骤至" + PROCESSTASK_STEP_NAME + "\n")
                    .append("，原因：" + REASON)
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤挂起提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单步骤" + PROCESSTASK_STEP_NAME + "已挂起\n")
                    .append("，原因：" + REASON)//TODO linbq步骤名称有替换成变量
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤暂停提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单步骤" + PROCESSTASK_STEP_NAME + "已暂停\n")
                    .append("，原因：" + REASON)
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "步骤失败提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单步骤" + PROCESSTASK_STEP_NAME + "已失败\n")
                    .append("，原因：" + REASON)
                    .append(PROCESSTASK_DETAILS_LINK)
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
     * 子流程触发点开始
     */
    //@Component
    public static class CreateSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "子任务创建提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "为工单步骤" + PROCESSTASK_STEP_NAME + "创建子任务：\n")
//                    .append("内容:" + SUBTASK_CONTENT + "\n")
//                    .append("子任务处理人:" + SUBTASK_WORKER + "\n")
                    .append("期望完成时间:【${DATA.subtaskdeadline}】\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    //@Component
    public static class EditSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "子任务更新提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "更新了子任务\n")
//                    .append("内容:" + SUBTASK_CONTENT + "\n")
//                    .append("子任务处理人:" + SUBTASK_WORKER + "\n")
                    .append("期望完成时间:【${DATA.subtaskdeadline}】\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    //@Component
    public static class AbortSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "子任务取消提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "取消子任务\n")
//                    .append("内容:" + SUBTASK_CONTENT + "\n")
//                    .append("子任务处理人:" + SUBTASK_WORKER + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    //@Component
    public static class RedoSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "子任务打回重做提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
//                    .append(PROCESSTASK_STEP_WORKER + "打回子任务给" + SUBTASK_WORKER + "重做\n")
//                    .append("内容:" + SUBTASK_CONTENT + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    //@Component
    public static class CompleteSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "子任务完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
//                    .append(SUBTASK_WORKER + "完成子任务\n")
//                    .append("内容:" + SUBTASK_CONTENT + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    //@Component
    public static class CompleteAllSubtask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + "所有子任务完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("所有子任务已全部完成\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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

    /**
     * 子流程触发点结束
     */
    /* 任务 触发点开始 */
    @Component
    public static class CreateTask extends EmailDefaultTemplateBase {

        @Override
        public String getTitle() {
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + "创建提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "为工单步骤" + PROCESSTASK_STEP_NAME + "创建" + TASK_CONFIG_NAME + "：\n")
                    .append("内容:" + TASK_CONTENT + "\n")
                    .append(TASK_CONFIG_NAME + "处理人:" + TASK_WORKER + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + "跟新提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "更新了" + TASK_CONFIG_NAME + "：\n")
                    .append("内容:" + TASK_CONTENT + "\n")
                    .append(TASK_CONFIG_NAME + "处理人:" + TASK_WORKER + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + "删除提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(PROCESSTASK_STEP_WORKER + "删除了" + TASK_CONFIG_NAME + "：\n")
                    .append("内容:" + TASK_CONTENT + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + TASK_CONFIG_NAME + "回复提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(TASK_WORKER + "回复" + TASK_CONFIG_NAME + "\n")
                    .append("回复内容:" + TASK_USER_CONTENT + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "所有任务已满足完成条件提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("所有任务已满足完成条件\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "工单超时提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("工单" + PROCESSTASK_SERIALNUMBER_TITLE + "即将超时\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更步骤处理人响应提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("变更步骤" + CHANGE_STEP_NAME + "已由" + CHANGE_STEP_WORKER + "开始处理\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更步骤完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + "完成变更步骤" + CHANGE_STEP_NAME + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更步骤取消提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + "取消变更步骤" + CHANGE_STEP_NAME + "\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更步骤新增评论提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append(CHANGE_STEP_WORKER + "新增了一条评论\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "所有变更步骤完成提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("所有变更步骤已完成\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更开始提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.ownername}】开始了变更\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更暂停提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.ownername}】暂停了变更\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更恢复提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.ownername}】恢复了变更\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
            return PROCESSTASK_SERIALNUMBER_TITLE + "变更重新开始提醒";
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append("【${DATA.ownername}】重新开始了变更\n")
                    .append(PROCESSTASK_DETAILS_LINK)
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
