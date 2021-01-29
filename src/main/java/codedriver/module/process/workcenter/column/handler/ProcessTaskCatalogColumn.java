package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.CatalogSqlTable;
import codedriver.framework.process.workcenter.table.ChannelSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcessTaskCatalogColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    CatalogMapper catalogMapper;

    @Override
    public String getName() {
        return "catalog";
    }

    @Override
    public String getDisplayName() {
        return "服务目录";
    }

    @Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        String catalogUuid = json.getString(this.getName());
        String catalogName = StringUtils.EMPTY;
        CatalogVo catalogVo = catalogMapper.getCatalogByUuid(catalogUuid);
        if (catalogVo != null) {
            catalogName = catalogVo.getName();
        }
        return catalogName;
    }

    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSort() {
        return 7;
    }

    @Override
    public Object getSimpleValue(Object json) {
        if (json != null) {
            return json.toString();
        }
        return null;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        if (processTaskVo.getChannelVo() != null) {
            if (processTaskVo.getChannelVo().getParent() != null) {
                return processTaskVo.getChannelVo().getParent().getName();
            } else {
                return "服务目录已被删除";
            }
        }
        return "服务已被删除";
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new CatalogSqlTable(), Arrays.asList(new SelectColumnVo(CatalogSqlTable.FieldEnum.UUID.getValue(), "catalogUuid")
                        , new SelectColumnVo(CatalogSqlTable.FieldEnum.NAME.getValue(), "catalogName"))));
                ;
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ChannelSqlTable(), new HashMap<String, String>() {{
                    put(ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue(), ChannelSqlTable.FieldEnum.UUID.getValue());
                }}));
                add(new JoinTableColumnVo(new ChannelSqlTable(), new CatalogSqlTable(), new HashMap<String, String>() {{
                    put(ChannelSqlTable.FieldEnum.PARENT_UUID.getValue(), CatalogSqlTable.FieldEnum.UUID.getValue());
                }}));
            }
        };
    }
}
