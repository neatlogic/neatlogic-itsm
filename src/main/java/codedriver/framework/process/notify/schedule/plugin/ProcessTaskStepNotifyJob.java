package codedriver.framework.process.notify.schedule.plugin;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;

@Component
public class ProcessTaskStepNotifyJob extends JobBase {

	@Override
	public Boolean checkCronIsExpired(JobObject jobObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadJob(JobObject jobObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initJob(String tenantUuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASKSTEPNOTIFY";
	}

}
