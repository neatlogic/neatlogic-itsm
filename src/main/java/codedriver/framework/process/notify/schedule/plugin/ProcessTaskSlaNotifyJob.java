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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadJob(JobObject jobObject) {
		String tenantUuid = jobObject.getTenantUuid();
		TenantContext.get().switchTenant(tenantUuid);
		Long slaId = (Long) jobObject.getData("slaId");
		String hash = jobObject.getData("hash").toString();
		ProcessTaskSlaTimeVo slaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
		if (slaTimeVo != null) {
			ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskNotifyVo(slaId, hash);
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
						JobObject newJobObject = new JobObject.Builder(slaId.toString(), this.getGroupName(), this.getClassName(), TenantContext.get().getTenantUuid()).withBeginTime(notifyDate.getTime()).withIntervalInSeconds(intervalTime).build();
						newJobObject.addData("slaId", slaId);
						newJobObject.addData("hash", hash);
						schedulerManager.loadJob(newJobObject);
					} catch (ParseException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	public void initJob(String tenantUuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		Long slaId = (Long) jobObject.getData("slaId");
		String hash = jobObject.getData("hash").toString();
		ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = processTaskMapper.getProcessTaskNotifyVo(slaId, hash);
		ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
		ProcessTaskSlaVo processTaskSlaVo = processTaskMapper.getProcessTaskSlaById(slaId);
		if (processTaskSlaVo != null && processTaskSlaTimeVo != null && processTaskSlaNotifyVo != null && processTaskSlaNotifyVo.getConfigObj() != null) {
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
					// 去掉成功和回退的步骤，其他步骤提醒需要处理
					if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.SUCCEED.getValue()) || processTaskStepVo.getStatus().equals(ProcessTaskStatus.ABORTED.getValue())) {
						it.remove();
					} else {
						// 找到所有未完成步骤的处理人
						workerList.addAll(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
					}
				}
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
			}
		}
	}

	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASKSTEPNOTIFY";
	}

}
