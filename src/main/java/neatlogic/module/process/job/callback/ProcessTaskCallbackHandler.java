/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.job.callback;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.autoexec.constvalue.JobStatus;
import neatlogic.framework.autoexec.dao.mapper.AutoexecJobMapper;
import neatlogic.framework.autoexec.dto.job.AutoexecJobVo;
import neatlogic.framework.autoexec.job.callback.core.AutoexecJobCallbackBase;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.process.constvalue.AutoExecJobProcessSource;
import neatlogic.framework.process.constvalue.AutoexecProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.automatic.FailPolicy;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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
                if (!JobStatus.PENDING.getValue().equals(autoexecJobVo.getStatus()) && !JobStatus.RUNNING.getValue().equals(autoexecJobVo.getStatus())) {
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
                    if (JobStatus.isRunningStatus(jobVo.getStatus())) {
                        return;
                    } else if (JobStatus.isCompletedStatus(jobVo.getStatus())) {
                        completed++;
                    } else if (JobStatus.isFailedStatus(jobVo.getStatus())) {
                        failed++;
                    }
                }
            }
            String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
            if (StringUtils.isNotBlank(config)) {
                failPolicy = (String) JSONPath.read(config, "autoexecConfig.failPolicy");
            }
            if (failed > 0) {
                if (FailPolicy.KEEP_ON.getValue().equals(failPolicy)) {
                    processTaskStepComplete(processTaskStepVo);
                }
            } else {
                processTaskStepComplete(processTaskStepVo);
            }
//            if (JobStatus.COMPLETED.getValue().equals(autoexecJobVo.getStatus())) {
//                processTaskStepComplete(processTaskStepVo, formAttributeDataList, hidecomponentList);
//            } else {
//                //暂停中、已暂停、中止中、已中止、已失败都属于异常，根据失败策略处理
//                if (FailPolicy.KEEP_ON.getValue().equals(failPolicy)) {
//                    processTaskStepComplete(processTaskStepVo, formAttributeDataList, hidecomponentList);
//                }
//            }
        }
    }

    private void processTaskStepComplete(ProcessTaskStepVo processTaskStepVo) {
        List<Long> toProcessTaskStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(processTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
        if (toProcessTaskStepIdList.size() == 1) {
            Long nextStepId = toProcessTaskStepIdList.get(0);
            IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(AutoexecProcessStepHandlerType.AUTOEXEC.getHandler());
            if (handler != null) {
                try {
                    JSONObject paramObj = processTaskStepVo.getParamObj();
                    paramObj.put("nextStepId", nextStepId);
                    paramObj.put("action", ProcessTaskOperationType.STEP_COMPLETE.getValue());
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
