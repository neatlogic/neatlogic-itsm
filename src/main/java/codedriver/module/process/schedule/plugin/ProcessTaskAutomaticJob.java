package codedriver.module.process.schedule.plugin;

import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.service.ProcessTaskService;

@Component
public class ProcessTaskAutomaticJob extends JobBase {
	@Autowired
	ProcessTaskService processTaskService;
	
	@Autowired
	ProcessTaskMapper processTaskMapper;
	
	@Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Autowired
	SchedulerMapper schedulerMapper;
	
	@Override
	public String getGroupName() {
		return TenantContext.get().getTenantUuid() + "-PROCESSTASK-AUTOMATIC";
	}

	@Override
	public Boolean checkCronIsExpired(JobObject jobObject) {
		return true;
	}

	@Override
	public void reloadJob(JobObject jobObject) {
		AutomaticConfigVo automaticConfigVo = (AutomaticConfigVo) jobObject.getData("automaticConfigVo");
		JSONObject data = (JSONObject) jobObject.getData("data");
		JobObject.Builder newJobObjectBuilder = null;
		JSONObject audit = null;
		JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
		Date startTime = TimeUtil.getDateByHourMinute(timeWindowConfig.getString("startTime"));
		Date endTime = TimeUtil.getDateByHourMinute(timeWindowConfig.getString("endTime"));
		String groupName = automaticConfigVo.getIsRequest()?"-REQUEST":"-CALLBACK";
		ProcessTaskStepVo  currentProcessTaskStepVo = (ProcessTaskStepVo) jobObject.getData("currentProcessTaskStepVo");
		newJobObjectBuilder = new JobObject.Builder(jobObject.getJobName(), this.getGroupName()+groupName, this.getClassName(), TenantContext.get().getTenantUuid())
				.withBeginTime(startTime)
			    .withEndTime(endTime)
			    .addData("data", data)
				.addData("automaticConfigVo", automaticConfigVo)
				.addData("currentProcessTaskStepVo", currentProcessTaskStepVo);
		if(automaticConfigVo.getIsRequest()) {
			newJobObjectBuilder.withIntervalInSeconds(5)
			                   .withRepeatCount(0);
			audit = data.getJSONObject("requestAudit");
		}else {
			newJobObjectBuilder.withIntervalInSeconds(automaticConfigVo.getCallbackInterval()*60);
			audit = data.getJSONObject("callbackAudit");
		}
		Date nextFireTime = schedulerManager.loadJob(newJobObjectBuilder.build());
		audit.put("nextFireTime",nextFireTime);
	}

	@Override
	public void initJob(String tenantUuid) {
		List<ProcessTaskStepDataVo> dataList = processTaskStepDataMapper.searchProcessTaskStepData(new ProcessTaskStepDataVo(null,null,ProcessStepHandler.AUTOMATIC.getHandler()));
		AutomaticConfigVo automaticConfigVo = null;
		for(ProcessTaskStepDataVo dataVo : dataList) {
			JSONObject dataObject = dataVo.getData();
			if(dataObject != null && dataObject.containsKey("requestAudit")) {
				JSONObject requestStatus = dataObject.getJSONObject("requestAudit").getJSONObject("status");
				
				if(!ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))) {
					//load第一次请求job
					initReloadJob(automaticConfigVo,dataVo,tenantUuid,true);
				}else if( dataObject.containsKey("callbackAudit")&&ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))){
					JSONObject callbackStatus = dataObject.getJSONObject("callbackAudit").getJSONObject("status");
					if(!ProcessTaskStatus.SUCCEED.getValue().equals(callbackStatus.getString("value"))
							&&!ProcessTaskStatus.FAILED.getValue().equals(callbackStatus.getString("value"))) {
						initReloadJob(automaticConfigVo,dataVo,tenantUuid,false);
					}
				}
			}
			
		}
	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		//String tenantUuid = jobObject.getTenantUuid();
		UserContext.init(null);//避免后续获取用户异常
		AutomaticConfigVo automaticConfigVo = (AutomaticConfigVo) jobObject.getData("automaticConfigVo");
		ProcessTaskStepVo currentProcessTaskStepVo = (ProcessTaskStepVo) jobObject.getData("currentProcessTaskStepVo");
		//excute
		Boolean isUnloadJob = processTaskService.runRequest(automaticConfigVo,currentProcessTaskStepVo);
		//update nextFireTime
		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessStepHandler.AUTOMATIC.getHandler());
		ProcessTaskStepDataVo stepData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
		JSONObject data = stepData.getData();// (JSONObject) jobObject.getData("data");
		if(data != null) {
			JSONObject audit = null;
			if(automaticConfigVo.getIsRequest()) {
				audit = data.getJSONObject("requestAudit");
			}else {
				audit = data.getJSONObject("callbackAudit");
			}
			if(context.getNextFireTime() != null) {
				audit.put("nextFireTime",context.getNextFireTime());
			}
			if(isUnloadJob){
				audit.remove("nextFireTime");
			}
			processTaskStepDataVo.setData(data.toJSONString());
			processTaskStepDataVo.setFcu("system");
			processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
		}
		//
		if(data == null || isUnloadJob) {
			schedulerManager.unloadJob(jobObject);
		}
	}
	
	/**
	 * reload 请求/回调job
	 * @param automaticConfigVo
	 * @param dataVo
	 * @param tenantUuid
	 * @param isRequest
	 */
	private void initReloadJob(AutomaticConfigVo automaticConfigVo ,ProcessTaskStepDataVo dataVo,String tenantUuid,Boolean isRequest) {
		ProcessTaskStepVo  processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(dataVo.getProcessTaskStepId());
		String config = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		JSONObject configJson = JSONObject.parseObject(config);
		if(configJson.containsKey("automaticConfig")) {
			automaticConfigVo = new AutomaticConfigVo(configJson.getJSONObject("automaticConfig"));
			automaticConfigVo.setIsRequest(isRequest);
			JobObject.Builder jobObjectBuilder = new JobObject.Builder(dataVo.getProcessTaskId()+"-"+dataVo.getProcessTaskStepId(),
					this.getGroupName(),
					this.getClassName(), 
					tenantUuid)
					.addData("data", dataVo.getData())
					.addData("automaticConfigVo", automaticConfigVo)
					.addData("currentProcessTaskStepVo", processTaskStepVo);
			JobObject jobObject = jobObjectBuilder.build();
			this.reloadJob(jobObject);
		}
	}
	
}
