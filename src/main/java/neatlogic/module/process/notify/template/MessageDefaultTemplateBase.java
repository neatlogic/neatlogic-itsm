package neatlogic.module.process.notify.template;

import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.process.notify.core.IDefaultTemplate;

import neatlogic.framework.util.$;
public abstract class MessageDefaultTemplateBase implements IDefaultTemplate {

    @Override
    public String getNotifyHandlerType() {
        return NotifyHandlerType.MESSAGE.getValue();
    }

    public static class Active extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.active.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.active.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.active.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.active.description.1");
        }

    }

    public static class Start extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.start.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.start.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.start.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.start.description.1");
        }

    }

    public static class Transfer extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.transfer.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.transfer.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.transfer.getcontent.2") + PROCESSTASK_STEP_WORKER + $.t("nmpnt.messagedefaulttemplatebase.transfer.getcontent.3"))
                    .append($.t("nmpnt.messagedefaulttemplatebase.transfer.getcontent.4"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.transfer.description.1");
        }

    }

    public static class Urge extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.urge.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.urge.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.urge.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.urge.description.1");
        }
    }

    public static class Succeed extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.succeed.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.succeed.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.succeed.getcontent.2"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.succeed.description.1");
        }
    }

    public static class Back extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.back.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.back.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.back.getcontent.2"))
                    .append($.t("nmpnt.messagedefaulttemplatebase.back.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.back.description.1");
        }
    }

    public static class Retreat extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.retreat.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.retreat.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.retreat.getcontent.2"))
                    .append($.t("nmpnt.messagedefaulttemplatebase.retreat.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.retreat.description.1");
        }
    }

    public static class Hang extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.hang.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.hang.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.hang.getcontent.2"))
                    .append($.t("nmpnt.messagedefaulttemplatebase.hang.getcontent.3"))//TODO linbq步骤名称有替换成变量
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.hang.description.1");
        }
    }

    public static class Abort extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.abort.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.abort.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "；<br>")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.abort.description.1");
        }
    }

    public static class Recover extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.recover.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.recover.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + "；<br>")
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.recover.description.1");
        }
    }

    public static class Failed extends MessageDefaultTemplateBase {

        @Override
        public String getTitle() {
            return $.t("nmpnt.messagedefaulttemplatebase.failed.gettitle.1") + PROCESSTASK_SERIALNUMBER_TITLE;
        }

        @Override
        public String getContent() {
            return new StringBuilder()
                    .append($.t("nmpnt.messagedefaulttemplatebase.failed.getcontent.1") + PROCESSTASK_SERIALNUMBER_TITLE + $.t("nmpnt.messagedefaulttemplatebase.failed.getcontent.2"))
                    .append($.t("nmpnt.messagedefaulttemplatebase.failed.getcontent.3"))
                    .append(getProcessTaskDetailsLink())
                    .toString();
        }

        @Override
        public String description() {
            return $.t("nmpnt.messagedefaulttemplatebase.failed.description.1");
        }
    }

}
