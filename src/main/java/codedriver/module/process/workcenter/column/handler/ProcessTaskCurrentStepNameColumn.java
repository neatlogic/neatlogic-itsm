package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
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
        return "当前步骤名";
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
                if ((ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) || (ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus()) && stepVo.getIsActive() == 1) || ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()))) {
                    stepNameList.add(stepVo.getName());
                }
            }
        }
        return stepNameList;
    }

}
