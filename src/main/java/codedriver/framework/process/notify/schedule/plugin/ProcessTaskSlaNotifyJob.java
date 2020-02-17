package codedriver.framework.process.notify.schedule.plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.notify.core.INotifyHandler;
import codedriver.framework.process.notify.core.NotifyHandlerFactory;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.notify.dao.mapper.NotifyMapper;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.dto.ProcessTaskSlaNotifyVo;
import codedriver.module.process.dto.ProcessTaskSlaTimeVo;
import codedriver.module.process.dto.ProcessTaskSlaVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.notify.dto.NotifyTemplateVo;
import codedriver.module.process.notify.dto.NotifyVo;

@Component
public class ProcessTaskSlaNotifyJob extends JobBase {
	static Logger logger = LoggerFactory.getLogger(ProcessTaskSlaNotifyJob.class);

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public Boolean checkCronIsExpired(JobObject jobObject) {
		Long slaTransferId = (Long) jobObject.getData("slaNotifyId");
		ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskNotifyById(slaTransferId);
		if (processTaskSlaNotifyVo == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void reloadJob(JobObject jobObject) {
		String tenantUuid = jobObject.getTenantUuid();
		TenantContext.get().switchTenant(tenantUuid);
		Long slaNotifyId = (Long) jobObject.getData("slaNotifyId");
		ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskNotifyById(slaNotifyId);
		boolean isJobLoaded = false;
		if (processTaskSlaNotifyVo != null) {
			ProcessTaskSlaTimeVo slaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(processTaskSlaNotifyVo.getSlaId());
			if (slaTimeVo != null) {
				if (processTaskSlaNotifyVo != null && processTaskSlaNotifyVo.getConfigObj() != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
					String expression = policyObj.getString("expression");
					int time = policyObj.getIntValue("time");
					String unit = policyObj.getString("unit");
					JSONArray notifyPluginList = policyObj.getJSONArray("pluginList");
					String executeType = policyObj.getString("executeType");
					int intervalTime = policyObj.getIntValue("intervalTime");
					String intervalUnit = policyObj.getString("intervalUnit");
					JSONArray receiverList = policyObj.getJSONArray("receiverList");
					if (executeType.equals("loop") && intervalTime > 0) {
						if (intervalUnit.equalsIgnoreCase("day")) {
							intervalTime = intervalTime * 24 * 60 * 60;
						} else {
							intervalTime = intervalTime * 60 * 60;
						}
					}
					if (StringUtils.isNotBlank(expression) && receiverList != null && receiverList.size() > 0 && notifyPluginList != null && notifyPluginList.size() > 0) {
						try {
							Date etdate = sdf.parse(slaTimeVo.getExpireTime());
							Calendar notifyDate = Calendar.getInstance();
							notifyDate.setTime(etdate);
							if (expression.equalsIgnoreCase("before")) {
								time = -time;
							}
							if (StringUtils.isNotBlank(unit) && time != 0) {
								if (unit.equalsIgnoreCase("day")) {
									notifyDate.add(time, Calendar.DAY_OF_MONTH);
								} else if (unit.equalsIgnoreCase("hour")) {
									notifyDate.add(time, Calendar.HOUR);
								} else {
									notifyDate.add(time, Calendar.MINUTE);
								}
							}
							JobObject.Builder newJobObjectBuilder = new JobObject.Builder(processTaskSlaNotifyVo.getId().toString(), this.getGroupName(), this.getClassName(), TenantContext.get().getTenantUuid()).withBeginTime(notifyDate.getTime()).withIntervalInSeconds(intervalTime).addData("slaNotifyId", processTaskSlaNotifyVo.getId());
							JobObject newJobObject = newJobObjectBuilder.build();
							Date triggerDate = schedulerManager.loadJob(newJobObject);
							if (triggerDate != null) {
								// 更新通知记录时间
								processTaskSlaNotifyVo.setTriggerTime(sdf.format(triggerDate));
								processTaskMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
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
			processTaskMapper.deleteProcessTaskSlaNotifyById(slaNotifyId);
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
		Long slaNotifyId = (Long) jobObject.getData("slaNotifyId");
		ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskNotifyById(slaNotifyId);
		if (processTaskSlaNotifyVo != null) {
			Long slaId = processTaskSlaNotifyVo.getSlaId();
			ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
			ProcessTaskSlaVo processTaskSlaVo = processTaskMapper.getProcessTaskSlaById(slaId);
			if (processTaskSlaVo != null && processTaskSlaTimeVo != null && processTaskSlaNotifyVo.getConfigObj() != null) {
				JSONObject policyObj = processTaskSlaNotifyVo.getConfigObj();
				JSONArray notifyPluginList = policyObj.getJSONArray("pluginList");
				JSONArray receiverList = policyObj.getJSONArray("receiverList");
				String templateUuid = policyObj.getString("template");
				NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(NotifyTriggerType.TIMEOUT);
				if (notifyPluginList != null && notifyPluginList.size() > 0 && receiverList != null && receiverList.size() > 0) {
					NotifyTemplateVo templateVo = notifyMapper.getNotifyTemplateByUuid(templateUuid);
					if (templateVo != null) {
						notifyBuilder.withTitleTemplate(templateVo.getTitle());
						notifyBuilder.withContentTemplate(templateVo.getContent());
					}
					/** 补充通知信息，将来有需要再补充 **/
					notifyBuilder.addData("sla", processTaskSlaVo).addData("slaTime", processTaskSlaTimeVo);
					Iterator<ProcessTaskStepVo> it = processTaskStepList.iterator();
					ProcessTaskVo processTaskVo = null;
					List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
					while (it.hasNext()) {
						ProcessTaskStepVo processTaskStepVo = it.next();
						if (processTaskVo == null) {
							processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskStepVo.getProcessTaskId());
						}
						// 未处理、处理中和挂起的步骤才需要计算SLA
						if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue()) || processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue()) || processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
							// 找到所有未完成步骤的处理人
							workerList.addAll(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
						} else {
							it.remove();
						}
					}
					/** 存在未完成步骤才发超时通知，否则清除通知作业 **/
					if (processTaskStepList.size() > 0) {
						notifyBuilder.addData("stepList", processTaskStepList);
						if (receiverList != null && receiverList.size() > 0) {
							for (int r = 0; r < receiverList.size(); r++) {
								String receiver = receiverList.getString(r);
								if (receiver.startsWith("common.")) {
									receiver = receiver.substring(7);
									if (receiver.equalsIgnoreCase("reporter")) {
										notifyBuilder.addUserId(processTaskVo.getReporter());
									} else if (receiver.equalsIgnoreCase("owner")) {
										notifyBuilder.addUserId(processTaskVo.getOwner());
									} else if (receiver.equalsIgnoreCase("worker")) {
										for (ProcessTaskStepWorkerVo workerVo : workerList) {
											notifyBuilder.addUserId(workerVo.getUserId());
										}
									}
								} else if (receiver.startsWith("user.")) {
									receiver = receiver.substring(5);
									notifyBuilder.addUserId(receiver);
								} else if (receiver.startsWith("team.")) {
									receiver = receiver.substring(5);
									notifyBuilder.addTeamId(receiver);
								}
							}
						}

						NotifyVo notifyVo = notifyBuilder.build();
						for (int i = 0; i < notifyPluginList.size(); i++) {
							String handler = notifyPluginList.getString(i);
							INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(handler);
							if (notifyHandler != null) {
								notifyHandler.execute(notifyVo);
							}
						}
						Date nextFireTime = context.getNextFireTime();
						if (nextFireTime != null) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							processTaskSlaNotifyVo.setTriggerTime(sdf.format(nextFireTime));
							processTaskMapper.updateProcessTaskSlaNotify(processTaskSlaNotifyVo);
						} else {
							// 删除通知记录
							processTaskMapper.deleteProcessTaskSlaNotifyById(processTaskSlaNotifyVo.getId());
						}
					} else {
						schedulerManager.unloadJob(jobObject);
						// 删除通知记录
						processTaskMapper.deleteProcessTaskSlaNotifyById(processTaskSlaNotifyVo.getId());
					}
				}
			} else {
				schedulerManager.unloadJob(jobObject);
				if (processTaskSlaNotifyVo != null) {
					// 删除通知记录
					processTaskMapper.deleteProcessTaskSlaNotifyById(processTaskSlaNotifyVo.getId());
				}
			}
		} else {
			schedulerManager.unloadJob(jobObject);
		}
	}

	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-NOTIFY";
	}

}
