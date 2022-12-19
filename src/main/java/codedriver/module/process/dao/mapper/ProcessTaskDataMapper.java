package codedriver.module.process.dao.mapper;

import java.util.List;
import java.util.Map;

public interface ProcessTaskDataMapper {
    Map<String, Object> getOne(String sql);

    List<Map<String, Object>> getList(String sql);

    Long getLong(String sql);
}
