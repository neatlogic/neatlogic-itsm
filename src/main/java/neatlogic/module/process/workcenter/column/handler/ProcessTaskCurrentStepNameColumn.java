package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskCurrentStepNameColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    TeamMapper teamMapper;

    @Override
    public String getName() {
        return "currentstepname";
    }

    @Override
    public String getDisplayName() {
        return "handler.processtask.column.currentstepname";
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
        return 5;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        List<String> stepNameList = (List<String>) getValue(processTaskVo);
        return String.join(",", stepNameList);
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<>();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
        List<String> stepNameList = new ArrayList<>();
        if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
            for (ProcessTaskStepVo stepVo : stepVoList) {
                if ((ProcessTaskStepStatus.DRAFT.getValue().equals(stepVo.getStatus()) || (ProcessTaskStepStatus.PENDING.getValue().equals(stepVo.getStatus()) && stepVo.getIsActive() == 1) || ProcessTaskStepStatus.RUNNING.getValue().equals(stepVo.getStatus()))) {
                    stepNameList.add(stepVo.getName());
                }
            }
        }
        return stepNameList;
    }

}
