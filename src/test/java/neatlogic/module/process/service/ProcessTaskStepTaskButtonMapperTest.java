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
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/** 验证生产查询真的读取旧按钮，防止服务代理测试掩盖 SQL 漏列的问题。 */
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
                    + "<mapper namespace=\"button.mapping.fixture\">" + select + "</mapper>";
            new XMLMapperBuilder(new StringReader(xml), configuration, resource + "#button-test", configuration.getSqlFragments()).parse();
        }
        MappedStatement mapped = configuration.getMappedStatement("button.mapping.fixture.getStepTaskUserListByStepTaskId");
        BoundSql boundSql = mapped.getBoundSql(101L);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String projection = sql.substring(sql.indexOf("select") + 6, sql.indexOf("from"));
        assertTrue("完成前查询必须读取已保存的 button: " + sql,
                projection.replace("`", "").matches("(?s).*\\bbutton\\b.*"));
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
            // JDK CachedRowSet 未实现 isClosed，用薄代理补齐 JDBC 生命周期查询，其余读取仍委托真实结果集。
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
            // 只替代 JDBC 边界，使用 MyBatis 的真实结果集处理器完成属性映射。
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
            List<Object> result = handler.handleResultSets(statement);
            assertEquals(1, result.size());
            assertEquals("同意", ((ProcessTaskStepTaskUserVo) result.get(0)).getButton());
        }
    }

    /** 不连接真实数据库，仅提供结果映射所需的 JDBC 方法。 */
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
