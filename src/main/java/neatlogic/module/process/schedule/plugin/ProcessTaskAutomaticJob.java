/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.schedule.plugin;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.process.constvalue.ProcessTaskStepDataType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.automatic.AutomaticConfigVo;
import neatlogic.framework.process.dto.automatic.ProcessTaskStepAutomaticRequestVo;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.enums.JobLoadTriggerType;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.service.ProcessTaskAutomaticService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import neatlogic.framework.util.$;
@Component
@DisallowConcurrentExecution
public class ProcessTaskAutomaticJob extends JobBase {
	@Override
	public String getName() {
		return $.t("nmpsp.processtaskautomaticjob.getname");
	}


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
    public Boolean isMyHealthy(JobObject jobObject) {
        return true;
    }

	@Override
	public void reloadJob(JobObject jobObject, JobLoadTriggerType triggerType) {
		Long requestId = Long.valueOf(jobObject.getJobName());
		ProcessTaskStepAutomaticRequestVo requestVo = processTaskMapper.getProcessTaskStepAutomaticRequestById(requestId);
		if (requestVo == null) {
			return;
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(requestVo.getProcessTaskStepId());
		if (processTaskStepVo == null) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			return;
		}
		String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		if (StringUtils.isBlank(stepConfig)) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			return;
		}
		JSONObject automaticConfig = (JSONObject) JSONPath.read(stepConfig, "automaticConfig");
		AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo(
				requestVo.getProcessTaskId(),
				requestVo.getProcessTaskStepId(),
				ProcessTaskStepDataType.AUTOMATIC.getValue(),
				SystemUser.SYSTEM.getUserUuid()
		);
		ProcessTaskStepDataVo stepData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
		JSONObject data = stepData.getData();
		String type = requestVo.getType();
		JobObject.Builder newJobObjectBuilder = new JobObject.Builder(
				jobObject.getJobName(),
				this.getGroupName() + "-" + type.toUpperCase(), this.getClassName(),
				TenantContext.get().getTenantUuid()
		);
		if("request".equals(type)) {
//			System.out.println("定时请求");
			JSONObject requestAudit = data.getJSONObject("requestAudit");
			newJobObjectBuilder.withBeginTime(requestAudit.getDate("startTime"))
					.withIntervalInSeconds(5)
					.withRepeatCount(0);
			Date nextFireTime = schedulerManager.loadJob(newJobObjectBuilder.build(), triggerType);
			requestAudit.put("nextFireTime",nextFireTime);
			requestVo.setTriggerTime(nextFireTime);
		} else {
//			System.out.println("定时回调");
			newJobObjectBuilder.withBeginTime(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10)))
					.withIntervalInSeconds(automaticConfigVo.getCallbackInterval()*60);
			Date nextFireTime = schedulerManager.loadJob(newJobObjectBuilder.build(), triggerType);
			JSONObject callbackAudit = data.getJSONObject("callbackAudit");
			callbackAudit.put("nextFireTime",nextFireTime);
			requestVo.setTriggerTime(nextFireTime);
		}
		processTaskMapper.updateProcessTaskStepAutomaticRequestTriggerTimeById(requestVo);
	}

	@Override
	public void initJob(String tenantUuid) {
		List<ProcessTaskStepAutomaticRequestVo> requestList = processTaskMapper.getAllProcessTaskStepAutomaticRequestList();
		for (ProcessTaskStepAutomaticRequestVo requestVo : requestList) {
			JobObject.Builder jobObjectBuilder = new JobObject.Builder(
					requestVo.getId().toString(),
					this.getGroupName(),
					this.getClassName(),
					TenantContext.get().getTenantUuid()
			);
			JobObject jobObject = jobObjectBuilder.build();
			this.reloadJob(jobObject, JobLoadTriggerType.SERVER_RESTART);
		}
	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		Long requestId = Long.valueOf(jobObject.getJobName());
		ProcessTaskStepAutomaticRequestVo requestVo = processTaskMapper.getProcessTaskStepAutomaticRequestById(requestId);
		if (requestVo == null) {
			schedulerManager.unloadJob(jobObject);
			return;
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(requestVo.getProcessTaskStepId());
		if (processTaskStepVo == null) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			schedulerManager.unloadJob(jobObject);
			return;
		}
		if (!Objects.equals(processTaskStepVo.getIsActive(), 1)) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			schedulerManager.unloadJob(jobObject);
			return;
		}
		if (!ProcessTaskStepStatus.PENDING.getValue().equals(processTaskStepVo.getStatus()) && !ProcessTaskStepStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			schedulerManager.unloadJob(jobObject);
			return;
		}
		String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		if (StringUtils.isBlank(stepConfig)) {
			processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
			schedulerManager.unloadJob(jobObject);
			return;
		}
		JSONObject automaticConfig = (JSONObject) JSONPath.read(stepConfig, "automaticConfig");
		AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
		JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
		int isTimeToRun = 0;
		//判断是否在时间窗口内
		if(MapUtils.isNotEmpty(timeWindowConfig)) {
			String startTime = timeWindowConfig.getString("startTime");
			String endTime = timeWindowConfig.getString("endTime");
			if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
				isTimeToRun = TimeUtil.isInTimeWindow(startTime, endTime);
			}
		}
		if(isTimeToRun == 0) {
			//避免后续获取用户异常
			UserContext.init(SystemUser.SYSTEM);
			ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserUuid());
			String type = requestVo.getType();
			if ("request".equals(type)) {
				processTaskAutomaticService.firstRequest(processTaskStepVo);
				processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
				ProcessTaskStepDataVo stepData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
				JSONObject data = stepData.getData();
				JSONObject requestAudit = data.getJSONObject("requestAudit");
				requestAudit.remove("nextFireTime");
				processTaskStepDataVo.setData(data.toJSONString());
				processTaskStepDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
				processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
			} else {
				boolean isUnloadJob = processTaskAutomaticService.callbackRequest(processTaskStepVo);
				ProcessTaskStepDataVo stepData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
				JSONObject data = stepData.getData();
				JSONObject callbackAudit = data.getJSONObject("callbackAudit");
				if(isUnloadJob){
					callbackAudit.remove("nextFireTime");
					processTaskMapper.deleteProcessTaskStepAutomaticRequestById(requestId);
					schedulerManager.unloadJob(jobObject);
				} else {
					Date nextFireTime = context.getNextFireTime();
					if(nextFireTime != null) {
						callbackAudit.put("nextFireTime",nextFireTime);
						requestVo.setTriggerTime(nextFireTime);
						processTaskMapper.updateProcessTaskStepAutomaticRequestTriggerTimeById(requestVo);
					}
				}

				processTaskStepDataVo.setData(data.toJSONString());
				processTaskStepDataVo.setFcu(SystemUser.SYSTEM.getUserUuid());
				processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
			}
		}
	}
}
