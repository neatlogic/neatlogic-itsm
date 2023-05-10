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

package neatlogic.module.process.schedule.plugin;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepTimerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2021/12/27 18:51
 **/
@Component
@DisallowConcurrentExecution
public class ProcessTaskStepTimerCompleteJob extends JobBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-TIMER-COMPLETE";
    }

    @Override
    public Boolean isMyHealthy(JobObject jobObject) {
        Long id = Long.valueOf(jobObject.getJobName());
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(id);
        if (processTaskStepTimerVo != null) {
            return true;
        }
        return false;
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        Long id = Long.valueOf(jobObject.getJobName());
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(id);
        if (processTaskStepTimerVo == null) {
            return;
        }
        Date beginTime = processTaskStepTimerVo.getTriggerTime();
        Date newDate = new Date();
        if (beginTime != null && newDate.after(beginTime)) {
            beginTime = newDate;
        }
        JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                id.toString(),
                this.getGroupName(),
                this.getClassName(),
                TenantContext.get().getTenantUuid()
        ).withBeginTime(beginTime)
                .withIntervalInSeconds(5)
                .withRepeatCount(0);
        Date nextFireTime = schedulerManager.loadJob(jobObjectBuilder.build());
        processTaskStepTimerVo.setTriggerTime(nextFireTime);
        processTaskMapper.updateProcessTaskStepTimerTriggerTimeById(processTaskStepTimerVo);
    }

    @Override
    public void initJob(String tenantUuid) {
        List<ProcessTaskStepTimerVo> processTaskStepTimerCompleteList = processTaskMapper.getAllProcessTaskStepTimerList();
        for (ProcessTaskStepTimerVo timerCompleteVo : processTaskStepTimerCompleteList) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    timerCompleteVo.getProcessTaskStepId().toString(),
                    this.getGroupName(),
                    this.getClassName(),
                    TenantContext.get().getTenantUuid()
            );
            JobObject jobObject = jobObjectBuilder.build();
            this.reloadJob(jobObject);
        }
    }

    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {
        Long id = Long.valueOf(jobObject.getJobName());
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(id);
        if (processTaskStepTimerVo == null) {
            schedulerManager.unloadJob(jobObject);
            return;
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepTimerVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            processTaskMapper.deleteProcessTaskStepAutomaticRequestById(id);
            schedulerManager.unloadJob(jobObject);
            return;
        }
        if (!Objects.equals(processTaskStepVo.getIsActive(), 1)) {
            processTaskMapper.deleteProcessTaskStepAutomaticRequestById(id);
            schedulerManager.unloadJob(jobObject);
            return;
        }
        IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (processStepHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processStepHandler.autoComplete(processTaskStepVo);
        processTaskMapper.deleteProcessTaskStepTimerByProcessTaskStepId(id);
        schedulerManager.unloadJob(jobObject);
    }
}
