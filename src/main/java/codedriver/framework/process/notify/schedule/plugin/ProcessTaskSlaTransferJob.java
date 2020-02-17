package codedriver.framework.process.notify.schedule.plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.dto.ProcessTaskSlaNotifyVo;
import codedriver.module.process.dto.ProcessTaskSlaTimeVo;
import codedriver.module.process.dto.ProcessTaskSlaTransferVo;
import codedriver.module.process.dto.ProcessTaskSlaVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;

@Component
public class ProcessTaskSlaTransferJob extends JobBase {
	static Logger logger = LoggerFactory.getLogger(ProcessTaskSlaTransferJob.class);

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private UserMapper userMapper;

	@Override
	public Boolean checkCronIsExpired(JobObject jobObject) {
		Long slaTransferId = (Long) jobObject.getData("slaTransferId");
		ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskMapper.getProcessTaskSlaTransferById(slaTransferId);
		if (processTaskSlaTransferVo == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void reloadJob(JobObject jobObject) {
		String tenantUuid = jobObject.getTenantUuid();
		TenantContext.get().switchTenant(tenantUuid);
		Long slaTransferId = (Long) jobObject.getData("slaTransferId");
		ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskMapper.getProcessTaskSlaTransferById(slaTransferId);
		boolean isJobLoaded = false;
		if (processTaskSlaTransferVo != null) {
			ProcessTaskSlaTimeVo slaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(processTaskSlaTransferVo.getSlaId());
			if (slaTimeVo != null) {
				if (processTaskSlaTransferVo != null && processTaskSlaTransferVo.getConfigObj() != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					JSONObject policyObj = processTaskSlaTransferVo.getConfigObj();
					String expression = policyObj.getString("expression");
					int time = policyObj.getIntValue("time");
					String unit = policyObj.getString("unit");
					String transferTo = policyObj.getString("transferTo");
					if (StringUtils.isNotBlank(expression) && StringUtils.isNotBlank(transferTo)) {
						try {
							Date etdate = sdf.parse(slaTimeVo.getExpireTime());
							Calendar transferDate = Calendar.getInstance();
							transferDate.setTime(etdate);
							if (expression.equalsIgnoreCase("before")) {
								time = -time;
							}
							if (StringUtils.isNotBlank(unit) && time != 0) {
								if (unit.equalsIgnoreCase("day")) {
									transferDate.add(time, Calendar.DAY_OF_MONTH);
								} else if (unit.equalsIgnoreCase("hour")) {
									transferDate.add(time, Calendar.HOUR);
								} else {
									transferDate.add(time, Calendar.MINUTE);
								}
							}
							JobObject.Builder newJobObjectBuilder = new JobObject.Builder(processTaskSlaTransferVo.getId().toString(), this.getGroupName(), this.getClassName(), TenantContext.get().getTenantUuid()).withBeginTime(transferDate.getTime()).addData("slaTransferId", processTaskSlaTransferVo.getId());
							JobObject newJobObject = newJobObjectBuilder.build();
							Date triggerDate = schedulerManager.loadJob(newJobObject);
							if (triggerDate != null) {
								// 更新通知记录时间
								processTaskSlaTransferVo.setTriggerTime(sdf.format(triggerDate));
								processTaskMapper.updateProcessTaskSlaTransfer(processTaskSlaTransferVo);
								isJobLoaded = true;
							}
						} catch (ParseException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		}
		if (!isJobLoaded) {
			// 没有加载到作业，则删除通知记录
			processTaskMapper.deleteProcessTaskSlaTransferById(slaTransferId);
		}
	}

	@Override
	public void initJob(String tenantUuid) {
		List<ProcessTaskSlaNotifyVo> slaNotifyList = processTaskMapper.getAllProcessTaskSlaNotify();
		for (ProcessTaskSlaNotifyVo processTaskSlaNotifyVo : slaNotifyList) {
			JobObject.Builder jobObjectBuilder = new JobObject.Builder(processTaskSlaNotifyVo.getSlaId().toString(), this.getGroupName(), this.getClassName(), TenantContext.get().getTenantUuid()).addData("slaNotifyId", processTaskSlaNotifyVo.getId());
			JobObject jobObject = jobObjectBuilder.build();
			this.reloadJob(jobObject);
		}
	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		Long slaTransferId = (Long) jobObject.getData("slaTransferId");
		ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskMapper.getProcessTaskSlaTransferById(slaTransferId);
		try {
			if (processTaskSlaTransferVo != null) {
				Long slaId = processTaskSlaTransferVo.getSlaId();
				ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
				List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
				ProcessTaskSlaVo processTaskSlaVo = processTaskMapper.getProcessTaskSlaById(slaId);
				if (processTaskSlaVo != null && processTaskSlaTimeVo != null && processTaskSlaTransferVo.getConfigObj() != null) {
					JSONObject policyObj = processTaskSlaTransferVo.getConfigObj();
					String transferTo = policyObj.getString("transferTo");
					if (StringUtils.isNotBlank(transferTo)) {
						UserVo userVo = userMapper.getUserBaseInfoByUserId(transferTo);
						ProcessTaskStepWorkerVo workerVo = null;
						if (userVo != null) {
							List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
							for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
								// 未处理、处理中和挂起的步骤才需要转交
								if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue()) || processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue()) || processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
									if (workerVo == null) {
										workerList.add(new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), transferTo));
									}
									IProcessStepHandler stepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
									if (stepHandler != null) {
										stepHandler.transfer(processTaskStepVo, workerList);
									}
								}
							}
						} else {
							throw new UserNotFoundException(transferTo);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			schedulerManager.unloadJob(jobObject);
			if (processTaskSlaTransferVo != null) {
				// 删除转交记录
				processTaskMapper.deleteProcessTaskSlaTransferById(processTaskSlaTransferVo.getId());
			}
		}
	}

	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-TRANSFER";
	}

}
