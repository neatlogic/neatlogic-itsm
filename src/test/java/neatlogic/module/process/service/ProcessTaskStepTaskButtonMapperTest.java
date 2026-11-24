package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessTaskStepTaskUserVo;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/** 验证完成操作和两处子任务表格的生产查询读取已保存按钮，防止 SQL 漏列或结果映射遗漏。 */
public class ProcessTaskStepTaskButtonMapperTest {
    /** 编译真实 select，并以 JDBC 内存结果集验证 MyBatis 自动映射到 button 属性。 */
    @Test
    public void completionQueryReadsAndMapsStoredButton() throws Exception {
        String resource = "neatlogic/module/process/dao/mapper/processtask/ProcessTaskStepTaskMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull("生产 Mapper XML 必须参与测试", stream);
            XPathParser parser = new XPathParser(stream, true, null, new XMLMapperEntityResolver());
            XNode select = parser.evalNode("/mapper/select[@id='getStepTaskUserListByStepTaskId']");
            assertNotNull(select);
            // 只装载待测查询，隔离生产 Mapper 的缓存初始化，不改写真实 SQL 和 resultType。
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                    + "<mapper namespace=\"button.mapping.fixture\">" + toXml(select) + "</mapper>";
            new XMLMapperBuilder(new StringReader(xml), configuration, resource + "#button-test", configuration.getSqlFragments()).parse();
        }
        MappedStatement mapped = configuration.getMappedStatement("button.mapping.fixture.getStepTaskUserListByStepTaskId");
        BoundSql boundSql = mapped.getBoundSql(101L);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        // 先校验 SQL 主体，避免 XML 夹具丢失文本时被 substring 异常掩盖。
        assertTrue("完成前查询应保留 SELECT/FROM: " + sql, sql.indexOf("select") >= 0 && sql.indexOf("from") > sql.indexOf("select"));
        String projection = sql.substring(sql.indexOf("select") + 6, sql.indexOf("from"));
        assertTrue("完成前查询必须读取已保存的 button: " + sql,
                projection.replace("`", "").matches("(?s).*\\bbutton\\b.*"));
        assertFalse("完成查询不能再引用已删除的显示字段", sql.contains("reply_display_mode"));
        assertEquals(ProcessTaskStepTaskUserVo.class, mapped.getResultMaps().get(0).getType());
        assertEquals(1, boundSql.getParameterMappings().size());

        try (CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet()) {
            RowSetMetaDataImpl metadata = new RowSetMetaDataImpl();
            metadata.setColumnCount(1);
            metadata.setColumnName(1, "button");
            metadata.setColumnLabel(1, "button");
            metadata.setColumnType(1, Types.VARCHAR);
            rows.setMetaData(metadata);
            rows.moveToInsertRow();
            rows.updateString(1, "同意");
            rows.insertRow();
            rows.moveToCurrentRow();
            rows.beforeFirst();
            // 与表格查询共用 JDBC 测试边界，仍执行真实 MyBatis 结果映射。
            List<Object> result = readResult(mapped, boundSql, rows);
            assertEquals(1, result.size());
            assertEquals("同意", ((ProcessTaskStepTaskUserVo) result.get(0)).getButton());
        }
    }

    /** 两处表格共用带用户关联的查询，验证 foreach 绑定及按钮原文、空值的显式映射。 */
    @Test
    public void tableQueryReadsAndMapsStoredButtons() throws Exception {
        String resource = "neatlogic/module/process/dao/mapper/processtask/ProcessTaskStepTaskMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull("生产 Mapper XML 必须参与测试", stream);
            XPathParser parser = new XPathParser(stream, true, null, new XMLMapperEntityResolver());
            XNode resultMap = parser.evalNode("/mapper/resultMap[@id='stepTaskUserMap']");
            XNode select = parser.evalNode("/mapper/select[@id='getStepTaskUserByStepTaskIdList']");
            assertNotNull(resultMap);
            assertNotNull(select);
            // 保留真实 association 和显式映射，避免只测 resultType 自动映射而漏掉表格数据问题。
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                    + "<mapper namespace=\"button.table.fixture\">" + toXml(resultMap) + toXml(select) + "</mapper>";
            new XMLMapperBuilder(new StringReader(xml), configuration, resource + "#button-table-test", configuration.getSqlFragments()).parse();
        }
        MappedStatement mapped = configuration.getMappedStatement("button.table.fixture.getStepTaskUserByStepTaskIdList");
        List<Long> taskIds = Arrays.asList(101L, 102L);
        BoundSql boundSql = mapped.getBoundSql(Collections.singletonMap("stepTaskIdList", taskIds));
        String sql = boundSql.getSql().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        // 带注释和 foreach 的真实查询也必须完整保留 SELECT/FROM 主体。
        assertTrue("表格查询应保留 SELECT/FROM: " + sql, sql.indexOf("select") >= 0 && sql.indexOf("from") > sql.indexOf("select"));
        String projection = sql.substring(sql.indexOf("select") + 6, sql.indexOf("from"));
        assertTrue("表格查询必须读取每条回复的 button: " + sql,
                projection.replace("`", "").matches("(?s).*\\bpstu\\.button\\b.*"));
        assertEquals(ProcessTaskStepTaskUserVo.class, mapped.getResultMaps().get(0).getType());
        assertFalse("表格查询不能再引用已删除的显示字段", sql.contains("reply_display_mode"));
        assertEquals(2, boundSql.getParameterMappings().size());
        for (int i = 0; i < taskIds.size(); i++) {
            assertEquals(taskIds.get(i), boundSql.getAdditionalParameter(boundSql.getParameterMappings().get(i).getProperty()));
        }

        // 有效名称不改写；历史空值仍为空，由前端统一显示占位符。
        for (String button : Arrays.asList("同意", "<b>Reply</b> & \"确认\"", null, "", "  ")) {
            try (CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet()) {
                RowSetMetaDataImpl metadata = new RowSetMetaDataImpl();
                metadata.setColumnCount(3);
                String[] names = {"id", "userUuid", "button"};
                for (int i = 0; i < names.length; i++) {
                    metadata.setColumnName(i + 1, names[i]);
                    metadata.setColumnLabel(i + 1, names[i]);
                    metadata.setColumnType(i + 1, i == 0 ? Types.BIGINT : Types.VARCHAR);
                }
                rows.setMetaData(metadata);
                rows.moveToInsertRow();
                rows.updateLong(1, 201L);
                rows.updateString(2, "reply-user");
                rows.updateString(3, button);
                rows.insertRow();
                rows.moveToCurrentRow();
                rows.beforeFirst();
                List<Object> result = readResult(mapped, boundSql, rows);
                assertEquals(1, result.size());
                ProcessTaskStepTaskUserVo reply = (ProcessTaskStepTaskUserVo) result.get(0);
                assertEquals(Long.valueOf(201L), reply.getId());
                assertEquals("reply-user", reply.getUserUuid());
                assertNotNull("关联用户与按钮应在同一回复行正常映射", reply.getUserVo());
                assertEquals("reply-user", reply.getUserVo().getUuid());
                assertEquals(button, reply.getButton());
            }
        }
    }

    /** 回复更新仅保存原始按钮和状态，任何分支均不能引用已删除的模式列。 */
    @Test
    public void updateDoesNotWriteRemovedReplyMode() throws Exception {
        String resource = "neatlogic/module/process/dao/mapper/processtask/ProcessTaskStepTaskMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream);
            XPathParser parser = new XPathParser(stream, true, null, new XMLMapperEntityResolver());
            XNode update = parser.evalNode("/mapper/update[@id='updateTaskUserById']");
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                    + "<mapper namespace=\"reply.mode.update.fixture\">" + toXml(update) + "</mapper>";
            new XMLMapperBuilder(new StringReader(xml), configuration, resource + "#reply-mode-update-test", configuration.getSqlFragments()).parse();
        }
        MappedStatement mapped = configuration.getMappedStatement("reply.mode.update.fixture.updateTaskUserById");
        ProcessTaskStepTaskUserVo reply = new ProcessTaskStepTaskUserVo(101L, "reply-user", "succeed");
        reply.setId(201L);
        reply.setButton("同意");
        BoundSql completed = mapped.getBoundSql(reply);
        assertFalse(completed.getSql().contains("reply_display_mode"));
        assertFalse(completed.getParameterMappings().stream().anyMatch(parameter -> "replyDisplayMode".equals(parameter.getProperty())));
        assertTrue(completed.getSql().contains("`button` = ?"));
        assertEquals("同意", configuration.newMetaObject(completed.getParameterObject()).getValue("button"));
        reply.setStatus(null);
        assertFalse(mapped.getBoundSql(reply).getSql().contains("`button` = ?"));
    }

    /** DOM 序列化保留注释间的全部 SQL 文本和动态标签，避免 XNode.toString 丢失混合内容。 */
    private String toXml(XNode node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node.getNode()), new StreamResult(writer));
        return writer.toString();
    }

    /** 仅代理 JDBC 生命周期，两个查询都通过真实结果集处理器验证属性及嵌套映射。 */
    private List<Object> readResult(MappedStatement mapped, BoundSql boundSql, CachedRowSet rows) throws Exception {
        // JDK CachedRowSet 未实现 isClosed，其余结果读取仍委托真实内存结果集。
        boolean[] closed = {false};
        ResultSet resultSet = proxy(ResultSet.class, (target, method, args) -> {
            if ("isClosed".equals(method.getName())) return closed[0];
            if ("close".equals(method.getName())) closed[0] = true;
            try {
                return method.invoke(rows, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        });
        DatabaseMetaData databaseMetaData = proxy(DatabaseMetaData.class, (target, method, args) -> {
            if ("supportsMultipleResultSets".equals(method.getName())) return false;
            throw new AssertionError(method.getName());
        });
        Connection connection = proxy(Connection.class, (target, method, args) -> {
            if ("getMetaData".equals(method.getName())) return databaseMetaData;
            throw new AssertionError(method.getName());
        });
        Statement statement = proxy(Statement.class, (target, method, args) -> {
            switch (method.getName()) {
                case "getResultSet": return resultSet;
                case "getConnection": return connection;
                case "getUpdateCount": return -1;
                default: throw new AssertionError(method.getName());
            }
        });
        DefaultResultSetHandler handler = new DefaultResultSetHandler(null, mapped, null, null, boundSql, RowBounds.DEFAULT);
        return handler.handleResultSets(statement);
    }

    /** 不连接真实数据库，仅提供结果映射所需的 JDBC 方法。 */
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
