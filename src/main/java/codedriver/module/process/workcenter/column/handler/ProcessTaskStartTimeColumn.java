package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class ProcessTaskStartTimeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getName() {
        return "starttime";
    }

    @Override
    public String getDisplayName() {
        return "上报时间";
    }

    @Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        return json.getString(this.getName());
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
        return 10;
    }

    @Override
    public Boolean getIsSort() {
        return true;
    }

    @Override
    public Object getSimpleValue(Object json) {
        if(json != null){
            return sdf.format((Date)json);
        }
        return null;
    }

    @Override
    public String getMySortSqlColumn(){
        return ProcessTaskSqlTable.FieldEnum.START_TIME.getValue();
    }

    @Override
    public ISqlTable getMySortSqlTable(){
        return new ProcessTaskSqlTable();
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>(){
            {
                add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.START_TIME.getValue())
                )));
            }
        };
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        return processTaskVo.getStartTime();
    }
}
