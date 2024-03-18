/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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
