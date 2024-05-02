/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StepTransferWorkerParamHandler extends ProcessTaskNotifyParamHandlerBase {

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
        return ProcessTaskStepNotifyParam.PROCESS_TASK_STEP_TRANSFER_WORKER.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskStepNotifyTriggerType.TRANSFER)) {
            return null;
        }
        List<ProcessTaskStepUserVo> stepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(stepUserList)) {
            List<String> userNameList = new ArrayList<>();
            List<String> userUuidList = stepUserList.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList());
            List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
            for (UserVo userVo : userList) {
                userNameList.add(userVo.getUserName() + "(" + userVo.getUserId() + ")");
            }
            return String.join("、", userNameList);
        } else {
            List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
            if (CollectionUtils.isNotEmpty(workerList)) {
                List<String> workerNameList = new ArrayList<>();
                for (ProcessTaskStepWorkerVo workerVo : workerList) {
                    if (Objects.equals(workerVo.getType(), ProcessUserType.MINOR.getValue())) {
                        continue;
                    }
                    if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                        UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                        if (userVo != null) {
                            workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                            workerVo.setName(userVo.getUserName());
                            workerNameList.add(userVo.getUserName() + "(" + userVo.getUserId() + ")");
                        }
                    } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                        TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                        if (teamVo != null) {
                            workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                            workerVo.setName(teamVo.getName());
                            workerNameList.add(teamVo.getName());
                        }
                    } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                        RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                        if (roleVo != null) {
                            workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                            workerVo.setName(roleVo.getName());
                            workerNameList.add(roleVo.getName());
                        }
                    }
                }
//                List<String> workerNameList = workerList.stream().filter(e -> e.getUserType().equals(ProcessUserType.MAJOR.getValue())).map(ProcessTaskStepWorkerVo::getName).collect(Collectors.toList());
                return String.join("、", workerNameList);
            }
        }
        return null;
    }
}
