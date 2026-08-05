/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */


package neatlogic.module.process.api.processtask.manualintervention;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.processtask.*;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@AuthAction(action = PROCESSTASK_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class UpdateProcessTaskStepStatusApi extends PrivateApiComponentBase {//

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "manualintervention/processtask/step/status/update";
    }

    @Override
    public String getName() {
        return "nmpapm.updateprocesstaskstepstatusapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepName", type = ApiParamType.STRING, desc = "term.itsm.processtaskstepname"),
            @Param(name = "processTaskNextStepName", type = ApiParamType.STRING, desc = "term.itsm.processtasknextstepname", help = "nmpapm.updateprocesstaskstepstatusapi.input.param.help.processtasknextstepname"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "term.itsm.processtaskstepid", help = "nmpapm.updateprocesstaskstepstatusapi.input.param.help.processtaskstepid"),
            @Param(name = "processTaskNextStepId", type = ApiParamType.LONG, desc = "term.itsm.processtasknextstepid", help = "nmpapm.updateprocesstaskstepstatusapi.input.param.help.processtasknextstepid"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "pending,running,succeed,hang", isRequired = true, desc = "common.status"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "common.userid"),
    })
    @Description(desc = "nmpapm.updateprocesstaskstepstatusapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        String processTaskStepName = jsonObj.getString("processTaskStepName");
        String processTaskNextStepName = jsonObj.getString("processTaskNextStepName");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        Long processTaskNextStepId = jsonObj.getLong("processTaskNextStepId");
        String status = jsonObj.getString("status");
        String userId = jsonObj.getString("userId");
        if (processTaskId == null && processTaskStepId == null) {
            throw new ParamNotExistsException("processTaskId", "processTaskStepId");
        }
        ProcessTaskStepVo processTaskStep;
        if (processTaskId != null) {
            if (StringUtils.isBlank(processTaskStepName)) {
                throw new ParamNotExistsException("processTaskStepName");
            }
            List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndStepName(new ProcessTaskStepVo(processTaskId, processTaskStepName));
            if (stepList.isEmpty()) {
                throw new ProcessTaskStepNotFoundException(processTaskStepName);
            }
            if (stepList.size() > 1) {
                throw new ProcessTaskStepFoundMultipleException(processTaskStepName);
            }
            processTaskStep = stepList.get(0);
        } else {
            processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStep == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId);
            }
        }
        if (StringUtils.isNotBlank(userId)) {
            UserVo user = userMapper.getUserByUserId(userId);
            if (user == null) {
                throw new UserNotFoundException(userId);
            }
            processTaskStep.setOriginalUserVo(user);
        }
        processTaskStep.setNextStepName(processTaskNextStepName);
        processTaskStep.setNextStepId(processTaskNextStepId);
        processTaskMapper.getProcessTaskLockById(processTaskStep.getProcessTaskId());
        map.get(status).accept(processTaskStep);
        return null;
    }

    static Map<String, Consumer<ProcessTaskStepVo>> map = new HashMap<>();

    @PostConstruct
    private void init() {
        map.put(ProcessTaskStepStatus.PENDING.getValue(), processTaskStepVo -> {
            if ("process".equals(processTaskStepVo.getType()) && processTaskStepVo.getOriginalUserVo() == null) {
                throw new ProcessTaskStepUserUnAssignException();
            }
            changeProcessTaskStepStatusToPending(processTaskStepVo);
        });
        map.put(ProcessTaskStepStatus.RUNNING.getValue(), processTaskStepVo -> {
            if ("process".equals(processTaskStepVo.getType())) {
                if (processTaskStepVo.getOriginalUserVo() == null) {
                    List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
                    // 需要处理人的步骤，不指定处理人时，旧处理人必须存在
                    if (processTaskStepUserList.isEmpty()) {
//                        throw new ProcessTaskStepUserUnAssignException();
                        IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
                        if (processStepHandler != null) {
                            try {
                                processStepHandler.assign(processTaskStepVo);
                            } catch (ProcessTaskException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        changeProcessTaskStepStatusToRunning(processTaskStepVo);
                    } else {
                        changeProcessTaskStepStatusToRunning(processTaskStepVo, new UserVo(processTaskStepUserList.get(0).getUserUuid(), processTaskStepUserList.get(0).getUserName()));
                    }
                } else {
                    processTaskMapper.deleteProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue()));
                    processTaskMapper.insertProcessTaskStepUser(new ProcessTaskStepUserVo(
                            processTaskStepVo.getProcessTaskId(),
                            processTaskStepVo.getId(),
                            processTaskStepVo.getOriginalUserVo().getUuid(),
                            ProcessUserType.MAJOR.getValue()
                    ));
                    changeProcessTaskStepStatusToRunning(processTaskStepVo, processTaskStepVo.getOriginalUserVo());
                }
            } else {
                changeProcessTaskStepStatusToRunning(processTaskStepVo);
            }
        });
        map.put(ProcessTaskStepStatus.SUCCEED.getValue(), processTaskStepVo -> {
            if (!ProcessStepHandlerType.END.getHandler().equals(processTaskStepVo.getHandler()) && StringUtils.isBlank(processTaskStepVo.getNextStepName())
                    && processTaskStepVo.getNextStepId() == null) {
                throw new ProcessTaskNextStepNameOrIdUnAssignException();
            }
            ProcessTaskStepVo nextStep = null;
            // 检查下一步骤是否合法
            if (StringUtils.isNotBlank(processTaskStepVo.getNextStepName())) {
                List<ProcessTaskStepVo> nextStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndStepName(new ProcessTaskStepVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getNextStepName()));
                if (nextStepList.isEmpty()) {
                    throw new ProcessTaskStepNotFoundException(processTaskStepVo.getNextStepName());
                }
                if (nextStepList.size() > 1) {
                    throw new ProcessTaskStepFoundMultipleException(processTaskStepVo.getNextStepName());
                }
                nextStep = nextStepList.get(0);
            } else if (processTaskStepVo.getNextStepId() != null) {
                nextStep = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepVo.getNextStepId());
                if (nextStep == null) {
                    throw new ProcessTaskStepNotFoundException(processTaskStepVo.getNextStepId());
                }
            }
            if (nextStep != null) {
                List<ProcessTaskStepRelVo> stepRelVoList = processTaskMapper.getProcessTaskStepRelByFromId(processTaskStepVo.getId());
                ProcessTaskStepVo finalNextStep = nextStep;
                if (stepRelVoList.stream().noneMatch(o -> Objects.equals(o.getToProcessTaskStepId(), finalNextStep.getId()))) {
                    throw new ProcessTaskNextStepIllegalException(processTaskStepVo.getName(), nextStep.getName());
                }
            }
            // 清空当前步骤worker
            processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue()));
            // 更改当前步骤处理人状态为DONE
            if (processTaskStepVo.getOriginalUserVo() == null) {
                ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
                processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
                processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
            } else {
                processTaskMapper.updateProcessTaskStepMajorUserAndStatus(new ProcessTaskStepUserVo(processTaskStepVo.getId()
                        , processTaskStepVo.getOriginalUserVo().getUuid()
                        , processTaskStepVo.getOriginalUserVo().getUserName()
                        , ProcessTaskStepUserStatus.DONE.getValue())
                );
            }
            // 更改当前步骤状态为SUCCEED
            processTaskStepVo.setIsActive(2);
            processTaskStepVo.setStatus(ProcessTaskStepStatus.SUCCEED.getValue());
            processTaskStepVo.setUpdateEndTime(1);
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            // 激活与下个节点之间的路径、更改工单状态
            if (ProcessStepHandlerType.END.getHandler().equals(processTaskStepVo.getHandler())) {
                processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.SUCCEED));
            } else if (nextStep != null) {
                if (ProcessStepHandlerType.END.getHandler().equals(nextStep.getHandler())) {
                    processTaskMapper.updateProcessTaskStepRelIsHit(new ProcessTaskStepRelVo(processTaskStepVo.getId(), nextStep.getId(), 1));
                    map.get(ProcessTaskStepStatus.SUCCEED.getValue()).accept(nextStep);
                } else {
                    processTaskMapper.updateProcessTaskStepRelIsHit(new ProcessTaskStepRelVo(processTaskStepVo.getId(), nextStep.getId(), 1));
                    map.get(ProcessTaskStepStatus.RUNNING.getValue()).accept(nextStep);
                }
            }
        });
        map.put(ProcessTaskStepStatus.HANG.getValue(), processTaskStepVo -> {
            ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
            processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
            processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
            processTaskStepVo.setIsActive(0);
            processTaskStepVo.setStatus(ProcessTaskStatus.HANG.getValue());
            processTaskStepVo.setUpdateEndTime(1);
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.HANG));
        });
    }

    /**
     * 更改步骤状态为待处理
     *
     * @param processTaskStep
     */
    private void changeProcessTaskStepStatusToPending(ProcessTaskStepVo processTaskStep) {
        if ("process".equals(processTaskStep.getType())) {
            processTaskMapper.deleteProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStep.getId(), ProcessUserType.MAJOR.getValue()));
            processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getId(), ProcessUserType.MAJOR.getValue()));
            processTaskMapper.insertIgnoreProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getProcessTaskId(), processTaskStep.getId()
                    , GroupSearch.USER.getValue(), processTaskStep.getOriginalUserVo().getUuid(), ProcessUserType.MAJOR.getValue()));
        }
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processTaskStep.getId(), ProcessTaskStepStatus.PENDING, 1);
        if (processTaskStep.getActiveTime() == null) {
            processTaskStepVo.setUpdateActiveTime(1);
        }
        processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
//        processTaskMapper.updateProcessTaskStepStatusByStepId(new ProcessTaskStepVo(processTaskStepVo.getId(), ProcessTaskStepStatus.PENDING, 1));
        processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.RUNNING));
    }

    /**
     * 更改无需处理人的步骤状态为处理中
     *
     * @param processTaskStep 步骤
     */
    private void changeProcessTaskStepStatusToRunning(ProcessTaskStepVo processTaskStep) {
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processTaskStep.getId(), ProcessTaskStepStatus.RUNNING, 1);
        if (processTaskStep.getActiveTime() == null) {
            processTaskStepVo.setUpdateActiveTime(1);
        }
        processTaskStepVo.setUpdateStartTime(1);
        processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
//        processTaskMapper.updateProcessTaskStepStatusByStepId(new ProcessTaskStepVo(processTaskStep.getId(), ProcessTaskStepStatus.RUNNING, 1));
        processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStep.getProcessTaskId(), ProcessTaskStatus.RUNNING));
    }

    /**
     * 更改需要处理人的步骤状态为处理中
     *
     * @param processTaskStep 步骤
     * @param majorUser       处理人
     */
    private void changeProcessTaskStepStatusToRunning(ProcessTaskStepVo processTaskStep, UserVo majorUser) {
        processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getId(), ProcessUserType.MAJOR.getValue()));
        processTaskMapper.insertIgnoreProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getProcessTaskId(), processTaskStep.getId(), GroupSearch.USER.getValue(), majorUser.getUuid(), ProcessUserType.MAJOR.getValue()));
        processTaskMapper.updateProcessTaskStepMajorUserAndStatus(new ProcessTaskStepUserVo(processTaskStep.getId(), majorUser.getUuid(), majorUser.getUserName(), ProcessTaskStepUserStatus.DOING.getValue()));
        changeProcessTaskStepStatusToRunning(processTaskStep);

    }

}
