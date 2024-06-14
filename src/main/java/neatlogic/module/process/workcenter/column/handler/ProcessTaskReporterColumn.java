package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.UserTable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessTaskReporterColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return "reporter";
    }

    @Override
    public String getDisplayName() {
        return "代报人";
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
        return 12;
    }

    @Override
    public Boolean getMyIsShow() {
        return false;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        Object value = getValue(processTaskVo);
        if (value != null) {
            return ((UserVo) value).getName();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        if (processTaskVo.getReporterVo() != null) {
            if (!(processTaskVo.getOwnerVo() != null
                    && Objects.equals(processTaskVo.getOwnerVo().getUuid(), processTaskVo.getReporterVo().getUuid()))) {
                return processTaskVo.getReporterVo();
            }
        }
        return null;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new UserTable(), "reporter", Arrays.asList(
                        new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(), "reporterUuid"),
                        new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(), "reporterName"),
                        new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(), "reporterInfo"),
                        new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(), "reporterVipLevel"),
                        new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(), "reporterPinYin"),
                        new SelectColumnVo(UserTable.FieldEnum.IS_ACTIVE.getValue(), "reporterIsActive"),
                        new SelectColumnVo(UserTable.FieldEnum.IS_DELETE.getValue(), "reporterIsDelete")
                )));
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new UserTable(), "reporter", new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.REPORTER.getValue(), UserTable.FieldEnum.UUID.getValue()));
                }}));
            }
        };
    }
}
