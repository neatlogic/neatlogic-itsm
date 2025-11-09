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

package neatlogic.module.process.job.callback;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.autoexec.constvalue.JobStatus;
import neatlogic.framework.autoexec.dao.mapper.AutoexecJobMapper;
import neatlogic.framework.autoexec.dto.job.AutoexecJobVo;
import neatlogic.framework.autoexec.job.callback.core.AutoexecJobCallbackBase;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.process.constvalue.AutoExecJobProcessSource;
import neatlogic.framework.process.constvalue.AutoexecProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.constvalue.automatic.FailPolicy;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2021/9/23 17:40
 **/
@Component
public class ProcessTaskCallbackHandler extends AutoexecJobCallbackBase {

    private final static Logger logger = LoggerFactory.getLogger(ProcessTaskCallbackHandler.class);

    @Resource
    private AutoexecJobMapper autoexecJobMapper;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;


    @Override
    public String getHandler() {
        return ProcessTaskCallbackHandler.class.getSimpleName();
    }

    @Override
    public Boolean getIsNeedCallback(AutoexecJobVo autoexecJobVo) {
        if (autoexecJobVo != null) {
            AutoexecJobVo autoexecJob = autoexecJobMapper.getJobInfo(autoexecJobVo.getId());
            if (AutoExecJobProcessSource.ITSM.getValue().equals(autoexecJob.getSource())) {
                if (JobStatus.isCompletedStatus(autoexecJobVo.getStatus()) || JobStatus.isFailedStatus(autoexecJobVo.getStatus())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doService(Long invokeId, AutoexecJobVo autoexecJobVo) {
        if (autoexecJobVo != null) {
            String failPolicy = FailPolicy.HANG.getValue();
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(invokeId);
            if (processTaskStepVo == null) {
                return;
            }
            int completed = 0, failed = 0;
            // 获取工单锁
            processTaskMapper.getProcessTaskLockById(processTaskStepVo.getProcessTaskId());
            // 获取与工单步骤关联的作业列表，如果其中存在作业状态为“待处理”和“处理中”的情况下，直接返回
            List<Long> jobIdList = autoexecJobMapper.getJobIdListByInvokeId(invokeId);
            if (CollectionUtils.isNotEmpty(jobIdList)) {
                List<AutoexecJobVo> autoexecJobList = autoexecJobMapper.getJobListByIdList(jobIdList);
                for (AutoexecJobVo jobVo : autoexecJobList) {
                    if (JobStatus.isCompletedStatus(jobVo.getStatus())) {
                        completed++;
                    } else if (JobStatus.isFailedStatus(jobVo.getStatus())) {
                        failed++;
                    } else {
                        return;
                    }
                }
            }
            String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
            if (StringUtils.isNotBlank(config)) {
                if (Objects.equals(processTaskStepVo.getHandler(), AutoexecProcessStepHandlerType.AUTOEXEC.getHandler())) {
                    failPolicy = (String) JSONPath.read(config, "autoexecConfig.failPolicy");
                } else if (Objects.equals(processTaskStepVo.getHandler(), "createjob")) {
                    failPolicy = (String) JSONPath.read(config, "createJobConfig.failPolicy");
                }
            }
            if (failed > 0) {
                if (FailPolicy.KEEP_ON.getValue().equals(failPolicy)) {
                    processTaskStepComplete(processTaskStepVo);
                } else {
                    IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
                    if (processStepHandler != null) {
                        try {
                            processStepHandler.assignAndUpdateStatus(processTaskStepVo);
                        } catch (ProcessTaskException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } else {
                processTaskStepComplete(processTaskStepVo);
            }
        }
    }

    private void processTaskStepComplete(ProcessTaskStepVo processTaskStepVo) {
        List<Long> toProcessTaskStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(processTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
        if (toProcessTaskStepIdList.size() == 1) {
            Long nextStepId = toProcessTaskStepIdList.get(0);
            IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (handler != null) {
                try {
                    JSONObject paramObj = processTaskStepVo.getParamObj();
                    paramObj.put("nextStepId", nextStepId);
                    paramObj.put("action", ProcessTaskStepOperationType.STEP_COMPLETE.getValue());
                    UserContext.init(SystemUser.SYSTEM);
                    handler.autoComplete(processTaskStepVo);
                } catch (ProcessTaskNoPermissionException e) {
                    logger.error(e.getMessage(), e);
//                throw new PermissionDeniedException();
                }
            }
        }
    }
}
