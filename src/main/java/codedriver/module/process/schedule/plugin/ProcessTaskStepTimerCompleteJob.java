/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.schedule.plugin;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepTimerVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.workcenter.table.UserTable;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
    public Boolean isHealthy(JobObject jobObject) {
        Long id = Long.valueOf(jobObject.getJobName());
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerById(id);
        if (processTaskStepTimerVo != null) {
            return true;
        }
        return false;
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        Long id = Long.valueOf(jobObject.getJobName());
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerById(id);
        if (processTaskStepTimerVo == null) {
            return;
        }
        Date beginTime = processTaskStepTimerVo.getTriggerTime();
        Date newDate = new Date();
        if (newDate.after(beginTime)) {
            beginTime = newDate;
        }
        JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                processTaskStepTimerVo.getId().toString(),
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
                    timerCompleteVo.getId().toString(),
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
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerById(id);
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
        if (!ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
            processTaskMapper.deleteProcessTaskStepAutomaticRequestById(id);
            schedulerManager.unloadJob(jobObject);
            return;
        }
        IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (processStepHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processStepHandler.complete(processTaskStepVo);
        processTaskMapper.deleteProcessTaskStepTimerById(id);
        schedulerManager.unloadJob(jobObject);
    }
}
