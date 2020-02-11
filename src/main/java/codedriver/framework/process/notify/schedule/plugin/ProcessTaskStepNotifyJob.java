package codedriver.framework.process.notify.schedule.plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import codedriver.framework.process.stephandler.core.ProcessStepHandlerUtilBase;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.process.dto.ProcessTaskSlaNotifyVo;
import codedriver.module.process.dto.ProcessTaskSlaTimeVo;

@Component
public class ProcessTaskStepNotifyJob extends JobBase {
	static Logger logger = LoggerFactory.getLogger(ProcessTaskStepNotifyJob.class);

	@Autowired
	private ProcessTaskMapper processTaskMapper;

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
				String template = policyObj.getString("template");
				JSONArray receiverList = policyObj.getJSONArray("receiverList");
				if (intervalTime > 0) {
					if (intervalUnit.equalsIgnoreCase("day")) {
						intervalTime = intervalTime * 24 * 60 * 60;
					} else {
						intervalTime = intervalTime * 60 * 60;
					}
				}
				if (StringUtils.isNotBlank(expression) && time > 0 && StringUtils.isNotBlank("unit") && notifyPluginList != null && notifyPluginList.size() > 0) {
					try {
						Date etdate = sdf.parse(slaTimeVo.getExpireTime());
						Calendar notifyDate = Calendar.getInstance();
						notifyDate.setTime(etdate);
						if (expression.equalsIgnoreCase("before")) {
							time = -time;
						}
						if (unit.equalsIgnoreCase("day")) {
							notifyDate.add(time, Calendar.DAY_OF_MONTH);
						} else if (unit.equalsIgnoreCase("hour")) {
							notifyDate.add(time, Calendar.HOUR);
						} else {
							notifyDate.add(time, Calendar.MINUTE);
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

	}

	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASKSTEPNOTIFY";
	}

}
