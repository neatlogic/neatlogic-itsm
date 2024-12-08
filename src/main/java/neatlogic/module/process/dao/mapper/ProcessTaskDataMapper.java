package neatlogic.module.process.dao.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProcessTaskDataMapper {
    Map<String, Object> getOne(String sql);

    List<Map<String, Object>> getList(String sql);

    Long getLong(String sql);

    int insertOne(@Param("tableName") String tableName, @Param("columnNameList") List<String> columnNameList, @Param("columnValueList") List<Object> columnValueList);
}
