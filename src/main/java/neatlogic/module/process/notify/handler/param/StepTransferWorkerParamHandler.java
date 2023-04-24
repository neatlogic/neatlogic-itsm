/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
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
