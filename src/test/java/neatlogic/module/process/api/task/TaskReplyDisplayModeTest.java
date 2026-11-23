package neatlogic.module.process.api.task;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.junit.Assert.*;

/** 通过真实策略保存入口验证模式校验和配置共存，不写入数据库。 */
public class TaskReplyDisplayModeTest {
    /** 旧调用及空白值缺省为分开，合法模式保存时不丢失自定义按钮和其他配置。 */
    @Test
    public void saveNormalizesModesAndPreservesOtherConfig() throws Exception {
        for (String mode : Arrays.asList(null, "", "  ", "separate", "combined")) {
            TaskConfigVo[] saved = {null};
            TaskSaveApi api = createApi(saved);
            JSONObject input = JSONObject.parseObject("{\"name\":\"会签\",\"num\":0,\"policy\":\"all\",\"isActive\":1}");
            JSONObject config = JSONObject.parseObject("{\"customButtonList\":[{\"name\":\"同意\"}],\"otherSetting\":true}");
            config.put("replyDisplayMode", mode);
            input.put("config", config);
            api.myDoService(input);
            assertNotNull(saved[0]);
            JSONObject persisted = JSONObject.parseObject(saved[0].getConfigStr());
            assertEquals("combined".equals(mode) ? "combined" : "separate", persisted.getString("replyDisplayMode"));
            assertEquals("同意", persisted.getJSONArray("customButtonList").getJSONObject(0).getString("name"));
            assertTrue(persisted.getBooleanValue("otherSetting"));
        }
    }

    /** 不带 config 的老调用仍可创建策略，编辑保存也沿用同一配置结构。 */
    @Test
    public void missingConfigAndEditingRemainCompatible() throws Exception {
        TaskConfigVo[] saved = {null};
        TaskSaveApi api = createApi(saved);
        api.myDoService(JSONObject.parseObject("{\"name\":\"征询意见\"}"));
        assertEquals("separate", saved[0].getConfig().getString("replyDisplayMode"));
        api.myDoService(JSONObject.parseObject("{\"id\":10,\"name\":\"征询意见\",\"config\":{\"replyDisplayMode\":\"combined\"}}"));
        assertEquals(Long.valueOf(10L), saved[0].getId());
        assertEquals("combined", saved[0].getConfig().getString("replyDisplayMode"));
    }

    /** 显式非法模式不得静默保存或触发持久化。 */
    @Test
    public void rejectsInvalidModesBeforeSaving() throws Exception {
        TaskConfigVo[] saved = {null};
        TaskSaveApi api = createApi(saved);
        for (String mode : Arrays.asList("COMBINED", "other", " combined ")) {
            JSONObject input = new JSONObject();
            JSONObject config = new JSONObject();
            config.put("replyDisplayMode", mode);
            input.put("config", config);
            try {
                api.myDoService(input);
                fail("Invalid mode should be rejected: " + mode);
            } catch (ParamIrregularException expected) {
                assertNull(saved[0]);
            }
        }
    }

    /** 只代理存取边界，校验、默认值及序列化均执行生产实现。 */
    private TaskSaveApi createApi(TaskConfigVo[] saved) {
        TaskSaveApi api = new TaskSaveApi();
        api.taskMapper = (TaskMapper) Proxy.newProxyInstance(TaskMapper.class.getClassLoader(), new Class<?>[]{TaskMapper.class}, (target, method, args) -> {
            switch (method.getName()) {
                case "checkTaskConfigNameIsRepeat": return 0;
                case "getTaskConfigById": return new TaskConfigVo();
                case "insertTaskConfig":
                case "updateTaskConfig": saved[0] = (TaskConfigVo) args[0]; return 1;
                default: throw new AssertionError(method.getName());
            }
        });
        return api;
    }
}
