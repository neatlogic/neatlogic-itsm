package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskStepTaskUserStatus;
import neatlogic.framework.process.dto.ProcessTaskStepTaskUserContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.process.task.TaskConfigManager;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTaskMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** 使用真实完成服务和内存依赖验证按钮审计；不连接数据库或发送实际通知。 */
public class ProcessTaskStepTaskButtonAuditTest {
    private static final Long TASK_ID = 101L;
    private static final Long STEP_ID = 201L;
    private static final Long PROCESS_ID = 301L;
    private static final String USER_UUID = "button-audit-user";
    private static final String HANDLER = "button-audit-test-handler";
    private final List<JSONObject> auditSnapshots = new ArrayList<>();
    private final List<Object> notifications = new ArrayList<>();
    private final List<Object> actions = new ArrayList<>();
    private ProcessTaskStepTaskServiceImpl service;
    private ProcessTaskStepTaskUserVo storedUser;
    private ProcessTaskStepTaskUserContentVo storedContent;
    private UserContext previousUserContext;
    private String previousMdcUserId;
    private Map<String, IProcessStepInternalHandler> handlers;
    private IProcessStepInternalHandler previousHandler;
    private int userUpdates;
    private int contentInserts;
    private int contentUpdates;
    private int workerRefreshes;
    private TaskConfigVo currentTaskConfig;

    /** 每次查询返回独立的步骤参数，模拟生产请求，不让上次审计参数污染下一次调用。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        previousUserContext = UserContext.get();
        previousMdcUserId = MDC.get("userId");
        UserVo user = new UserVo();
        user.setUuid(USER_UUID);
        user.setUserId(USER_UUID);
        UserContext.init(user, null, "+00:00");
        storedUser = new ProcessTaskStepTaskUserVo(TASK_ID, USER_UUID, ProcessTaskStepTaskUserStatus.PENDING.getValue());
        storedUser.setId(401L);
        storedUser.setIsDelete(0);
        service = new ProcessTaskStepTaskServiceImpl() {
            // 本组测试聚焦完成后的变更和审计，权限与代办分支由原服务负责。
            @Override
            public int checkIsReplyable(ProcessTaskVo task, ProcessTaskStepVo step, String userUuid, Long taskUserId) {
                return 1;
            }

            @Override
            public void stepMinorUserRegulate(ProcessTaskStepTaskUserVo taskUser) {
                // 测试用户直接处理自己的子任务，无需代办关系。
            }
        };
        // 策略查询独立于历史回复，便于验证修改配置不会重写已保存快照。
        currentTaskConfig = new TaskConfigVo();
        currentTaskConfig.setConfig(new JSONObject());
        service.taskMapper = proxy(TaskMapper.class, (target, method, args) -> {
            if ("getTaskConfigById".equals(method.getName())) return currentTaskConfig;
            throw new AssertionError("Unexpected strategy mapper call: " + method.getName());
        });
        service.processTaskMapper = proxy(ProcessTaskMapper.class, (target, method, args) -> {
            switch (method.getName()) {
                case "getProcessTaskLockById": return null;
                case "insertIgnoreProcessTaskContent": return 1;
                default: throw new AssertionError("Unexpected process mapper call: " + method.getName());
            }
        });
        service.processTaskStepTaskMapper = proxy(ProcessTaskStepTaskMapper.class, (target, method, args) -> {
            switch (method.getName()) {
                case "getStepTaskDetailById": return newTask();
                case "getStepTaskUserListByStepTaskId": return Collections.singletonList(copyUser());
                case "getStepTaskUserContentByStepTaskUserId": return storedContent;
                case "insertTaskUserContent":
                    storedContent = (ProcessTaskStepTaskUserContentVo) args[0];
                    storedContent.setId(501L);
                    contentInserts++;
                    return 1;
                case "updateTaskUserContentById":
                    storedContent = (ProcessTaskStepTaskUserContentVo) args[0];
                    contentUpdates++;
                    return 1;
                case "updateTaskUserById":
                    ProcessTaskStepTaskUserVo update = (ProcessTaskStepTaskUserVo) args[0];
                    storedUser.setStatus(update.getStatus());
                    storedUser.setUserUuid(update.getUserUuid());
                    storedUser.setButton(update.getButton());
                    userUpdates++;
                    return 1;
                case "getStepTaskWithUserByProcessTaskStepId": return Collections.singletonList(newTask());
                default: throw new AssertionError("Unexpected task mapper call: " + method.getName());
            }
        });
        service.processTaskService = proxy(ProcessTaskService.class, (target, method, args) -> {
            if ("checkProcessTaskParamsIsLegal".equals(method.getName())) {
                ProcessTaskStepVo step = new ProcessTaskStepVo();
                step.setId(STEP_ID);
                step.setProcessTaskId(PROCESS_ID);
                step.setHandler(HANDLER);
                ProcessTaskVo task = new ProcessTaskVo(PROCESS_ID);
                task.setCurrentProcessTaskStep(step);
                return task;
            }
            throw new AssertionError("Unexpected task service call: " + method.getName());
        });
        service.taskConfigManager = new TaskConfigManager();
        service.taskConfigManager.actionDispatcherInit();
        Field utilField = ProcessTaskStepTaskServiceImpl.class.getDeclaredField("processStepHandlerUtil");
        utilField.setAccessible(true);
        utilField.set(service, proxy(IProcessStepHandlerUtil.class, (target, method, args) -> {
            switch (method.getName()) {
                case "audit":
                    assertEquals(ProcessTaskAuditType.COMPLETETASK, args[1]);
                    // 序列化参数快照，模拟提交后落库，不持有后续可变的步骤对象。
                    auditSnapshots.add(JSONObject.parseObject(((ProcessTaskStepVo) args[0]).getParamObj().toJSONString()));
                    return null;
                case "notify": notifications.add(args[1]); return null;
                case "action": actions.add(args[1]); return null;
                default: throw new AssertionError("Unexpected step utility call: " + method.getName());
            }
        }));
        Field handlersField = ProcessStepInternalHandlerFactory.class.getDeclaredField("componentMap");
        handlersField.setAccessible(true);
        handlers = (Map<String, IProcessStepInternalHandler>) handlersField.get(null);
        previousHandler = handlers.put(HANDLER, proxy(IProcessStepInternalHandler.class, (target, method, args) -> {
            if ("updateProcessTaskStepUserAndWorker".equals(method.getName())) {
                workerRefreshes++;
                return null;
            }
            throw new AssertionError("Unexpected step handler call: " + method.getName());
        }));
    }

    /** 恢复线程上下文和静态处理器注册，避免影响同一 JVM 的其他测试。 */
    @After
    public void tearDown() {
        if (handlers != null) {
            if (previousHandler == null) {
                handlers.remove(HANDLER);
            } else {
                handlers.put(HANDLER, previousHandler);
            }
        }
        if (previousUserContext == null) {
            if (UserContext.get() != null) {
                UserContext.get().release();
            }
        } else {
            UserContext.init(previousUserContext);
        }
        if (previousMdcUserId == null) {
            MDC.remove("userId");
        } else {
            MDC.put("userId", previousMdcUserId);
        }
    }

    /** 首次完成保存描述、按钮和来源，并触发正常的完成后处理。 */
    @Test
    public void firstCompletionRecordsContentAndButton() throws Exception {
        assertEquals(TASK_ID, service.completeTask(TASK_ID, "<p>testaaaa</p>", "同意", "pc"));
        assertEquals(1, contentInserts);
        assertEquals(1, userUpdates);
        assertEquals("同意", storedUser.getButton());
        assertEquals(1, auditSnapshots.size());
        assertSnapshot(0, "<p>testaaaa</p>", "同意");
        assertEquals("pc", auditSnapshots.get(0).getString("source"));
        assertNormalCompletionCount(1);
    }

    /** 仅按钮变化也是有效更新，原有审计快照保持原按钮名称。 */
    @Test
    public void buttonOnlyChangeRecordsAnotherSnapshot() throws Exception {
        service.completeTask(TASK_ID, "unchanged", "同意", "pc");
        service.completeTask(TASK_ID, "unchanged", "驳回", "pc");
        assertEquals(2, userUpdates);
        assertEquals(1, contentInserts);
        assertEquals(0, contentUpdates);
        assertEquals("驳回", storedUser.getButton());
        assertEquals(2, auditSnapshots.size());
        assertSnapshot(0, "unchanged", "同意");
        assertSnapshot(1, "unchanged", "驳回");
        assertNormalCompletionCount(2);
    }

    /** 修改描述时继续记录当前按钮，不能丢掉原有内容更新行为。 */
    @Test
    public void contentOnlyChangeRetainsCurrentButton() throws Exception {
        service.completeTask(TASK_ID, "first", "回复", "pc");
        service.completeTask(TASK_ID, "second", "回复", "pc");
        assertEquals(1, contentUpdates);
        assertEquals(2, userUpdates);
        assertEquals(2, auditSnapshots.size());
        assertSnapshot(0, "first", "回复");
        assertSnapshot(1, "second", "回复");
    }

    /** 相同内容和按钮重复提交不再次更新完成状态、审计、通知或动作。 */
    @Test
    public void identicalSubmissionDoesNotRepeatCompletion() throws Exception {
        service.completeTask(TASK_ID, "unchanged", "确认", "pc");
        service.completeTask(TASK_ID, "unchanged", "确认", "pc");
        assertEquals(1, userUpdates);
        assertEquals(1, contentInserts);
        assertEquals(0, contentUpdates);
        assertEquals(1, auditSnapshots.size());
        assertNormalCompletionCount(1);
    }

    /** 兼容未传和空白按钮；每次新内容都有审计，但不保留上一操作的按钮参数。 */
    @Test
    public void absentAndBlankButtonsDoNotCreateEmptyDetails() throws Exception {
        service.completeTask(TASK_ID, "first", "同意", "pc");
        String[] buttons = {null, "", " \t\n "};
        for (int i = 0; i < buttons.length; i++) {
            String content = "legacy reply " + i;
            service.completeTask(TASK_ID, content, buttons[i], "pc");
            JSONObject snapshot = auditSnapshots.get(i + 1);
            assertEquals(content, snapshot.getString("content"));
            assertFalse(snapshot.containsKey("button"));
            assertFalse(snapshot.containsKey("oldButton"));
        }
        assertEquals(4, auditSnapshots.size());
        assertSnapshot(0, "first", "同意");
    }

    /** 按钮保存原文而非 HTML 转义文本，展示层负责按纯文本渲染。 */
    @Test
    public void buttonSnapshotPreservesLiteralCharacters() throws Exception {
        String button = "<b>同意 & Approve \"A\"</b>";
        service.completeTask(TASK_ID, "content", button, "pc");
        assertSnapshot(0, "content", button);
        assertEquals(button, storedUser.getButton());
    }

    /** 完成时生成当次回复意见，同时保留原始内容和按钮供业务流程使用。 */
    @Test
    public void firstCompletionBuildsCombinedOpinion() throws Exception {
        currentTaskConfig.getConfig().put("replyDisplayMode", "combined");
        service.completeTask(TASK_ID, "<p>意见</p>", "同意", "pc");
        assertEquals("combined", auditSnapshots.get(0).getString("replyDisplayMode"));
        assertFalse(auditSnapshots.get(0).containsKey("oldReplyDisplayMode"));
        assertSnapshot(0, "<p>意见</p>", "同意");
        assertEquals("<p>同意。意见</p>", auditSnapshots.get(0).getString("replyOpinion"));
    }

    /** 模式变化本身不触发完成，实际编辑采用新模式，原审计仍保留旧快照。 */
    @Test
    public void strategyChangeRequiresEffectiveReplyChange() throws Exception {
        service.completeTask(TASK_ID, "first", "同意", "pc");
        currentTaskConfig.getConfig().put("replyDisplayMode", "combined");
        service.completeTask(TASK_ID, "first", "同意", "pc");
        assertEquals(1, auditSnapshots.size());
        assertNormalCompletionCount(1);
        service.completeTask(TASK_ID, "second", "同意", "pc");
        assertEquals("separate", auditSnapshots.get(0).getString("replyDisplayMode"));
        assertEquals("combined", auditSnapshots.get(1).getString("replyDisplayMode"));
        assertSnapshot(0, "first", "同意");
        assertSnapshot(1, "second", "同意");
        assertEquals("first", auditSnapshots.get(0).getString("replyOpinion"));
        assertEquals("同意。second", auditSnapshots.get(1).getString("replyOpinion"));
        assertNormalCompletionCount(2);
    }

    /** 缺失策略、旧配置和异常历史模式均采用兼容默认值。 */
    @Test
    public void missingAndLegacyStrategiesDefaultToSeparate() throws Exception {
        currentTaskConfig = null;
        service.completeTask(TASK_ID, "missing strategy", "回复", "pc");
        currentTaskConfig = new TaskConfigVo();
        service.completeTask(TASK_ID, "missing config", "回复", "pc");
        currentTaskConfig.setConfig(JSONObject.parseObject("{\"replyDisplayMode\":\"unexpected\"}"));
        service.completeTask(TASK_ID, "legacy value", "回复", "pc");
        for (JSONObject snapshot : auditSnapshots) {
            assertEquals("separate", snapshot.getString("replyDisplayMode"));
            assertEquals(snapshot.getString("content"), snapshot.getString("replyOpinion"));
        }
    }

    /** 同一子任务不同处理人的审计意见独立保存，原始按钮仍各自保留。 */
    @Test
    public void multipleRespondersRetainTheirOwnOpinions() throws Exception {
        service.completeTask(TASK_ID, "first user", "确认", "pc");
        ProcessTaskStepTaskUserVo firstUser = storedUser;
        UserVo second = new UserVo();
        second.setUuid("second-reply-user");
        second.setUserId("second-reply-user");
        UserContext.init(second, null, "+00:00");
        storedUser = new ProcessTaskStepTaskUserVo(TASK_ID, second.getUuid(), ProcessTaskStepTaskUserStatus.PENDING.getValue());
        storedUser.setId(402L);
        storedUser.setIsDelete(0);
        storedContent = null;
        currentTaskConfig.getConfig().put("replyDisplayMode", "combined");
        service.completeTask(TASK_ID, "second user", "回复", "pc");
        assertEquals("separate", auditSnapshots.get(0).getString("replyDisplayMode"));
        assertEquals("combined", auditSnapshots.get(1).getString("replyDisplayMode"));
        assertEquals("first user", auditSnapshots.get(0).getString("replyOpinion"));
        assertEquals("回复。second user", auditSnapshots.get(1).getString("replyOpinion"));
        assertEquals("确认", firstUser.getButton());
        assertEquals("回复", storedUser.getButton());
    }

    /** 验证同一条操作的描述和按钮快照，不要求前端展示旧值比较。 */
    private void assertSnapshot(int index, String content, String button) {
        JSONObject snapshot = auditSnapshots.get(index);
        assertEquals(content, snapshot.getString("content"));
        assertEquals(button, snapshot.getString("button"));
        assertFalse(snapshot.containsKey("oldButton"));
    }

    /** 有效更新沿用正常完成流程，完全重复提交不能增加这些调用。 */
    private void assertNormalCompletionCount(int count) {
        assertEquals(count, Collections.frequency(notifications, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK));
        assertEquals(count, Collections.frequency(notifications, ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK));
        assertEquals(count, Collections.frequency(actions, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK));
        assertEquals(count, Collections.frequency(actions, ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK));
        assertEquals(count, workerRefreshes);
    }

    /** 模拟任务查询返回的数据，并使用真实的所有处理人完成策略。 */
    private ProcessTaskStepTaskVo newTask() {
        ProcessTaskStepTaskVo task = new ProcessTaskStepTaskVo();
        task.setId(TASK_ID);
        task.setProcessTaskId(PROCESS_ID);
        task.setProcessTaskStepId(STEP_ID);
        task.setTaskConfigId(601L);
        task.setTaskConfigName("测试子任务");
        task.setTaskConfigPolicy("all");
        task.setStepTaskUserVoList(Collections.singletonList(copyUser()));
        return task;
    }

    /** 查询返回脱离内存存储的用户对象，避免更新代理影响服务正在比较的旧值。 */
    private ProcessTaskStepTaskUserVo copyUser() {
        ProcessTaskStepTaskUserVo user = new ProcessTaskStepTaskUserVo(TASK_ID, storedUser.getUserUuid(), storedUser.getStatus());
        user.setId(storedUser.getId());
        user.setIsDelete(storedUser.getIsDelete());
        user.setButton(storedUser.getButton());
        return user;
    }

    /** 沿用仓库已有的 JDK 代理测试方式，不引入额外 mock 依赖。 */
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
