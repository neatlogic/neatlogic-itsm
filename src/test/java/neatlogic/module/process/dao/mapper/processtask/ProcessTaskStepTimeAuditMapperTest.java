package neatlogic.module.process.dao.mapper.processtask;

import neatlogic.framework.process.dto.ProcessTaskStepTimeAuditVo;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.*;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

/** 使用生产 Mapper 和 MyBatis 生成 SQL；不模拟数据库执行结果。 */
public class ProcessTaskStepTimeAuditMapperTest {
    private Configuration configuration;
    private static final String NS = ProcessTaskStepTimeAuditMapper.class.getName() + ".";
    private static final String[] FIELDS = {"ActiveTime", "StartTime", "AbortTime", "CompleteTime", "BackTime", "PauseTime"};
    private static final String[] COLUMNS = {"active_time", "start_time", "abort_time", "complete_time", "back_time", "pause_time"};

    /** 加载实际 XML，解析错误会直接使测试失败。 */
    @Before public void setup() throws Exception {
        configuration = new Configuration();
        String resource = "neatlogic/module/process/dao/mapper/processtask/ProcessTaskStepTimeAuditMapper.xml";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream);
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    /** 取真实动态 SQL，并统一空白以忽略排版。 */
    private String sql(String statement, Object arg) {
        return configuration.getMappedStatement(NS + statement).getBoundSql(arg).getSql().replaceAll("\\s+", " ").trim();
    }

    /** 六种时间字段的全部组合必须一一对应，尤其不能退化为 choose 只选一个。 */
    @Test public void insertSupportsAll64FieldCombinations() throws Exception {
        for (int mask = 0; mask < 64; mask++) {
            ProcessTaskStepTimeAuditVo row = new ProcessTaskStepTimeAuditVo();
            row.setId(7L); row.setProcessTaskStepId(9L);
            List<String> columns = new ArrayList<>(Arrays.asList("`id`", "`processtask_step_id`"));
            List<String> values = new ArrayList<>(Arrays.asList("?", "?"));
            for (int bit = 0; bit < 6; bit++) if ((mask & (1 << bit)) != 0) {
                row.getClass().getMethod("set" + FIELDS[bit], String.class).invoke(row, "now");
                columns.add("`" + COLUMNS[bit] + "`"); values.add("NOW(3)");
            }
            String expected = "INSERT INTO `processtask_step_timeaudit` (" + String.join(",", columns)
                    + ") VALUES (" + String.join(",", values) + ")";
            assertEquals("字段组合 " + mask, expected.replaceAll("\\s+", ""), sql("insertProcessTaskStepTimeAudit", row).replaceAll("\\s+", ""));
            BoundSql bound = configuration.getMappedStatement(NS + "insertProcessTaskStepTimeAudit").getBoundSql(row);
            assertEquals(Arrays.asList("id", "processTaskStepId"), bound.getParameterMappings().stream().map(p -> p.getProperty()).collect(Collectors.toList()));
        }
    }

    /** 更新审计必须绑定旧行 ID、所属步骤及正确的时间列。 */
    @Test public void updateBindsStepAndAuditIdentity() throws Exception {
        for (int i = 0; i < 6; i++) {
            ProcessTaskStepTimeAuditVo row = new ProcessTaskStepTimeAuditVo();
            row.setId(7L); row.setProcessTaskStepId(9L);
            row.getClass().getMethod("set" + FIELDS[i], String.class).invoke(row, "now");
            String actual = sql("updateProcessTaskStepTimeAudit", row).replaceAll("\\s+", "");
            assertEquals("UPDATE`processtask_step_timeaudit`SET`processtask_step_id`=?,`" + COLUMNS[i] + "`=NOW(3)WHERE`id`=?", actual);
            BoundSql bound = configuration.getMappedStatement(NS + "updateProcessTaskStepTimeAudit").getBoundSql(row);
            assertEquals(Arrays.asList("processTaskStepId", "id"), bound.getParameterMappings().stream().map(p -> p.getProperty()).collect(Collectors.toList()));
            assertEquals(9L, configuration.newMetaObject(bound.getParameterObject()).getValue("processTaskStepId"));
            assertEquals(7L, configuration.newMetaObject(bound.getParameterObject()).getValue("id"));
        }
    }

    /** 查询必须携带步骤身份和审计顺序，避免不同步骤的开放区间互相截断。 */
    @Test public void slaQueryPreservesStepIdentityAndAuditOrder() {
        String actual = sql("getProcessTaskStepTimeAuditBySlaId", 8L);
        assertTrue(actual.contains("a.`processtask_step_id` AS processTaskStepId"));
        assertTrue(actual.contains("ON a.`processtask_step_id` = b.`processtask_step_id`"));
        assertTrue(actual.endsWith("ORDER BY a.`id`"));
        assertTrue(actual.contains("WHERE b.`sla_id` = ?"));
    }
}

