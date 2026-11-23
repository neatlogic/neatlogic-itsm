package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.dto.ProcessTaskContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 运行真实审计线程与明细类型工厂，仅代理持久化边界，不连接数据库或发送通知。 */
public class ProcessTaskReplyOpinionAuditThreadTest {
    private final List<ProcessTaskStepAuditDetailVo> details = new ArrayList<>();
    private final Map<String, String> contents = new HashMap<>();
    private Field mapperField;
    private Object previousMapper;
    private UserContext previousUser;
    private String previousUserId;
    private int auditCount;

    /** 保留并替换静态持久化依赖，验证真正写入的类型、内容哈希及审计次数。 */
    @Before
    public void setUp() throws Exception {
        previousUser = UserContext.get();
        previousUserId = MDC.get("userId");
        UserVo user = new UserVo();
        user.setUuid("reply-opinion-test-user");
        user.setUserId("reply-opinion-test-user");
        UserContext.init(user, null, "+00:00");
        mapperField = ProcessTaskAuditThread.class.getDeclaredField("processTaskMapper");
        mapperField.setAccessible(true);
        previousMapper = mapperField.get(null);
        ProcessTaskMapper mapper = (ProcessTaskMapper) Proxy.newProxyInstance(ProcessTaskMapper.class.getClassLoader(),
                new Class<?>[]{ProcessTaskMapper.class}, (target, method, args) -> {
                    switch (method.getName()) {
                        case "getProcessTaskStepBaseInfoById":
                        case "getProcessTaskById": return null;
                        case "insertProcessTaskStepAudit":
                            ((ProcessTaskStepAuditVo) args[0]).setId(701L);
                            auditCount++;
                            return 1;
                        case "insertIgnoreProcessTaskContent":
                            ProcessTaskContentVo content = (ProcessTaskContentVo) args[0];
                            contents.put(content.getHash(), content.getContent());
                            return 1;
                        case "insertProcessTaskStepAuditDetail":
                            details.add((ProcessTaskStepAuditDetailVo) args[0]);
                            return 1;
                        default: throw new AssertionError(method.getName());
                    }
                });
        mapperField.set(null, mapper);
    }

    /** 恢复共享上下文，避免影响后续服务回归测试。 */
    @After
    public void tearDown() throws Exception {
        if (mapperField != null) mapperField.set(null, previousMapper);
        if (previousUser == null) UserContext.get().release();
        else UserContext.init(previousUser);
        if (previousUserId == null) MDC.remove("userId");
        else MDC.put("userId", previousUserId);
    }

    /** 拼接模式只保存压缩意见；通知、动作仍能读取原始内容与操作。 */
    @Test
    public void combinedPersistsOnlyOpinionWithoutMutatingSharedParams() {
        JSONObject params = params("combined", "<p>同意。原始回复</p>");
        JSONObject before = new JSONObject();
        before.putAll(params);
        run(params, ProcessTaskAuditType.COMPLETETASK);
        assertEquals(1, auditCount);
        assertEquals(Arrays.asList("replyopinion"), types());
        assertEquals("<p>同意。原始回复</p>", contents.get(details.get(0).getNewContent()));
        assertNull(details.get(0).getOldContent());
        assertEquals(before, params);
        assertEquals("<p>原始回复</p>", params.getString("content"));
        assertEquals("同意", params.getString("button"));
    }

    /** 分开模式意见位于操作前，旧 content 与模式不重复落入审计。 */
    @Test
    public void separatePersistsOpinionAndLiteralButton() {
        JSONObject params = params("separate", "<p>原始回复</p>");
        params.put("button", "<b>同意 & \"A\"</b>");
        run(params, ProcessTaskAuditType.COMPLETETASK);
        details.sort(null);
        assertEquals(Arrays.asList("replyopinion", "button"), types());
        assertEquals("<p>原始回复</p>", contents.get(details.get(0).getNewContent()));
        assertEquals("<b>同意 & \"A\"</b>", details.get(1).getNewContent());
        assertNull(details.get(0).getOldContent());
        assertNull(details.get(1).getOldContent());
    }

    /** 空意见和空白操作不会产生占位明细，显式 null 仍标识新版完成事件。 */
    @Test
    public void emptyOpinionAndWhitespaceButtonDoNotPersistEmptyDetails() {
        JSONObject params = params("separate", null);
        params.put("button", " \t\u00a0\u202f\ufeff ");
        run(params, ProcessTaskAuditType.COMPLETETASK);
        assertEquals(1, auditCount);
        assertTrue(details.isEmpty());
        assertTrue(params.containsKey("replyOpinion"));
        assertTrue(params.containsKey("content"));
    }

    /** 创建子任务及旧版完成调用继续使用原始 content/button，不新增意见或模式。 */
    @Test
    public void legacyAndCreationAuditsKeepOriginalDetails() {
        for (ProcessTaskAuditType action : Arrays.asList(ProcessTaskAuditType.CREATETASK, ProcessTaskAuditType.COMPLETETASK)) {
            details.clear();
            JSONObject params = new JSONObject();
            params.put("content", "旧回复");
            params.put("button", "回复");
            assertSame(params, ProcessTaskAuditThread.getDetailParams(params, action));
            run(params, action);
            details.sort(null);
            assertEquals(Arrays.asList("content", "button"), types());
            assertEquals("旧回复", contents.get(details.get(0).getNewContent()));
        }
        JSONObject creation = params("combined", "不应处理");
        assertSame(creation, ProcessTaskAuditThread.getDetailParams(creation, ProcessTaskAuditType.CREATETASK));
    }

    /** 模拟步骤中已有旧值，确认新版完成不记录旧意见或污染其他业务参数。 */
    private JSONObject params(String mode, String opinion) {
        JSONObject params = new JSONObject();
        params.put("content", "<p>原始回复</p>");
        params.put("oldContent", "旧内容");
        params.put("button", "同意");
        params.put("oldButton", "旧按钮");
        params.put("replyDisplayMode", mode);
        params.put("oldReplyDisplayMode", "separate");
        params.put("replyOpinion", opinion);
        params.put("oldReplyOpinion", "旧意见");
        return params;
    }

    /** 直接执行线程避免启动真实线程池；参数对象按生产行为保持共享引用。 */
    private void run(JSONObject params, ProcessTaskAuditType action) {
        ProcessTaskStepVo step = new ProcessTaskStepVo();
        step.setId(201L);
        step.setProcessTaskId(301L);
        step.getParamObj().putAll(params);
        // 保留显式 null 键，不能通过默认 JSON 序列化丢掉新版完成事件标记。
        JSONObject before = new JSONObject();
        before.putAll(step.getParamObj());
        new ProcessTaskAuditThread(step, action).execute();
        assertEquals(before, step.getParamObj());
    }

    private List<String> types() {
        return details.stream().map(ProcessTaskStepAuditDetailVo::getType).collect(Collectors.toList());
    }
}
