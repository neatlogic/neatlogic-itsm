/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class StepWorkerParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.STEPWORKER.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
        if (CollectionUtils.isNotEmpty(workerList)) {
            for (ProcessTaskStepWorkerVo workerVo : workerList) {
                if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                    UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                    if (userVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                        workerVo.setName(userVo.getUserName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                    TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                    if (teamVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                        workerVo.setName(teamVo.getName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                    RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                    if (roleVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                        workerVo.setName(roleVo.getName());
                    }
                }
            }
            List<String> workerNameList = workerList.stream().filter(e -> e.getUserType().equals(ProcessUserType.MAJOR.getValue())).map(ProcessTaskStepWorkerVo::getName).collect(Collectors.toList());
            return String.join("、", workerNameList);
        } else {
            List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), null);
            List<String> userNameList = userList.stream().filter(e -> e.getUserType().equals(ProcessUserType.MAJOR.getValue())).map(e -> e.getUserName()).collect(Collectors.toList());
            return String.join("、", userNameList);
        }
    }
}
