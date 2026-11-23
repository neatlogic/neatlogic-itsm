package neatlogic.module.process.api.processtask;

import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditVo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/** 验证时间线查询对内部快照的过滤，不需要启动服务或连接数据库。 */
public class ProcessTaskReplyDisplayModeAuditTest {
    /** 内部明细不暴露给展示列表，内容和操作的快照仍保持独立原文。 */
    @Test
    public void removesLegacyInternalDetailWithoutChangingContent() {
        ProcessTaskStepAuditVo audit = new ProcessTaskStepAuditVo();
        ProcessTaskStepAuditDetailVo content = new ProcessTaskStepAuditDetailVo(1L, "content", null, "<p>意见</p>");
        ProcessTaskStepAuditDetailVo button = new ProcessTaskStepAuditDetailVo(1L, "button", null, "同意");
        audit.setAuditDetailList(new ArrayList<>(Arrays.asList(content, button,
                new ProcessTaskStepAuditDetailVo(1L, "replydisplaymode", null, "combined"))));
        ProcessTaskAuditListApi.removeInternalReplyDisplayMode(audit);
        assertEquals(Arrays.asList(content, button), audit.getAuditDetailList());
        assertEquals("<p>意见</p>", content.getNewContent());
        assertEquals("同意", button.getNewContent());
        assertTrue(ProcessTaskAuditDetailType.REPLYOPINION.getNeedCompression());
        assertEquals(4, ProcessTaskAuditDetailType.REPLYOPINION.getSort());
        assertTrue(ProcessTaskAuditDetailType.REPLYOPINION.getSort() < ProcessTaskAuditDetailType.BUTTON.getSort());
    }

    /** 旧审计、空明细及异常历史快照按分开显示，不依赖当前策略。 */
    @Test
    public void legacyAndInvalidModesAreHidden() {
        ProcessTaskStepAuditVo oldAudit = new ProcessTaskStepAuditVo();
        ProcessTaskAuditListApi.removeInternalReplyDisplayMode(oldAudit);
        for (String mode : Arrays.asList(null, "", "unexpected", "separate")) {
            oldAudit.setAuditDetailList(new ArrayList<>(Arrays.asList(
                    new ProcessTaskStepAuditDetailVo(1L, "replydisplaymode", null, mode))));
            ProcessTaskAuditListApi.removeInternalReplyDisplayMode(oldAudit);
            assertTrue(oldAudit.getAuditDetailList().isEmpty());
        }
    }
}
