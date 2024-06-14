package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ISqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Integer getSort() {
        return 15;
    }

    @Override
    public Boolean getIsSort() {
        return true;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        return sdf.format(processTaskVo.getStartTime());
    }

    @Override
    public String getMySortSqlColumn(Boolean isColumn){
        return String.format(" %s.%s",getMySortSqlTable().getShortName() , ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
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
