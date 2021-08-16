package codedriver.module.process.schedule.plugin;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.processtask.AutomaticConfigException;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.service.ProcessTaskAutomaticService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@DisallowConcurrentExecution
public class ProcessTaskAutomaticJob extends JobBase {
	@Resource
	ProcessTaskAutomaticService processTaskAutomaticService;
	
	@Resource
	ProcessTaskMapper processTaskMapper;
	
	@Resource
	ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;
	
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
		if(automaticConfigVo == null){
			throw new AutomaticConfigException(jobObject.getJobName());
		}
		JSONObject data = (JSONObject) jobObject.getData("data");
		JobObject.Builder newJobObjectBuilder = null;
		JSONObject audit = null;
		//默认以当前时间为开始时间
		Date startTime = new Date(System.currentTimeMillis());
		/** 计算开始时间 **/
		JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
		if(MapUtils.isNotEmpty(timeWindowConfig)) {
			int isTimeToRun = TimeUtil.isInTimeWindow(timeWindowConfig.getString("startTime"),timeWindowConfig.getString("endTime"));
			startTime = TimeUtil.getDateByHourMinute(timeWindowConfig.getString("startTime"),isTimeToRun>0?1:0);
		}
		String groupName = automaticConfigVo.getIsRequest()?"-REQUEST":"-CALLBACK";
		ProcessTaskStepVo  currentProcessTaskStepVo = (ProcessTaskStepVo) jobObject.getData("currentProcessTaskStepVo");
		newJobObjectBuilder = new JobObject.Builder(jobObject.getJobName(), this.getGroupName()+groupName, this.getClassName(), TenantContext.get().getTenantUuid())
				.withBeginTime(startTime)
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
		List<ProcessTaskStepDataVo> dataList = processTaskStepDataMapper.searchProcessTaskStepData(new ProcessTaskStepDataVo(null,null,ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserId()));
		AutomaticConfigVo automaticConfigVo = null;
		for(ProcessTaskStepDataVo dataVo : dataList) {
			JSONObject dataObject = dataVo.getData();
			if(dataObject != null && dataObject.containsKey("requestAudit")) {
				JSONObject requestStatus = dataObject.getJSONObject("requestAudit").getJSONObject("status");
				if(ProcessTaskStatus.PENDING.getValue().equals(requestStatus.getString("value"))) {
					//load第一次请求job
					initReloadJob(automaticConfigVo,dataVo,tenantUuid,true);
				}else if(ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))&&dataObject.containsKey("callbackAudit")){
					JSONObject callbackStatus = dataObject.getJSONObject("callbackAudit").getJSONObject("status");
					if(ProcessTaskStatus.PENDING.getValue().equals(callbackStatus.getString("value"))) {
						initReloadJob(automaticConfigVo,dataVo,tenantUuid,false);
					}
				}
			}
			
		}
	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		AutomaticConfigVo automaticConfigVo = (AutomaticConfigVo) jobObject.getData("automaticConfigVo");
		JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
		Integer isTimeToRun = 0;
		//判断是否在时间窗口内
		if(timeWindowConfig != null) {
			isTimeToRun = TimeUtil.isInTimeWindow(timeWindowConfig.getString("startTime"),timeWindowConfig.getString("endTime"));
		}
		if(isTimeToRun == 0) {
			//避免后续获取用户异常
			UserContext.init(SystemUser.SYSTEM.getUserVo(), SystemUser.SYSTEM.getTimezone());
			ProcessTaskStepVo currentProcessTaskStepVo = (ProcessTaskStepVo) jobObject.getData("currentProcessTaskStepVo");
			//excute
			Boolean isUnloadJob = processTaskAutomaticService.runRequest(automaticConfigVo,currentProcessTaskStepVo);
			//update nextFireTime
			ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserId());
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
				processTaskStepDataVo.setFcu(SystemUser.SYSTEM.getUserId());
				processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
			}
			//
			if(data == null || isUnloadJob) {
				schedulerManager.unloadJob(jobObject);
			}
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
		String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
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
