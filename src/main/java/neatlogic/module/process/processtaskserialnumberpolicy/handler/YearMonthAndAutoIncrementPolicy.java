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

package neatlogic.module.process.processtaskserialnumberpolicy.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.enums.JobLoadTriggerType;
import neatlogic.module.process.service.ProcessTaskSerialNumberService;
import org.quartz.CronExpression;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@Service
public class YearMonthAndAutoIncrementPolicy implements IProcessTaskSerialNumberPolicyHandler {
    private Logger logger = LoggerFactory.getLogger(YearMonthAndAutoIncrementPolicy.class);

    @Resource
    private ProcessTaskSerialNumberService processTaskSerialNumberService;

    @Override
    public String getName() {
        return "年月 + 自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
        return processTaskSerialNumberService.makeupFormAttributeList(10, 16);
    }

    @Override
    public JSONObject makeupConfig(JSONObject jsonObj) {
        return processTaskSerialNumberService.makeupConfig(jsonObj, 6);
    }

    @Override
    public String genarate(String channelTypeUuid) {
        return processTaskSerialNumberService.genarate(channelTypeUuid, new SimpleDateFormat("yyyyMM"));
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialNumberService.batchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, new SimpleDateFormat("yyyyMM"));
    }


    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialNumberService.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, true, Date.from(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    @Component
    @DisallowConcurrentExecution
    private static class ProcessTaskSerialNumberSeedResetJob extends JobBase {
        @Override
        public String getName() {
            return "工单月度流水号重置";
        }

        private String cron = "0 0 0 1 * ?"; // 每月1日0时0分0秒

        @Autowired
        private ProcessTaskSerialNumberService processTaskSerialNumberService;

        @Override
        public String getGroupName() {
            return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SERIALNUMBERSEED-" + YearMonthAndAutoIncrementPolicy.class.getSimpleName() + "-RESET";
        }

        @Override
        public Boolean isMyHealthy(JobObject jobObject) {
            return true;
        }

        @Override
        public void reloadJob(JobObject jobObject, JobLoadTriggerType triggerType) {
            String tenantUuid = jobObject.getTenantUuid();
            TenantContext.get().switchTenant(tenantUuid);
            if (CronExpression.isValidExpression(cron)) {
                JobObject.Builder newJobObjectBuilder =
                        new JobObject.Builder(jobObject.getJobName(), this.getGroupName(), this.getClassName(),
                                TenantContext.get().getTenantUuid()).withCron(cron);
                JobObject newJobObject = newJobObjectBuilder.build();
                schedulerManager.loadJob(newJobObject, triggerType);
            }
        }

        @Override
        public void initJob(String tenantUuid) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    this.getGroupName(),
                    this.getGroupName(),
                    this.getClassName(),
                    TenantContext.get().getTenantUuid());
            JobObject jobObject = jobObjectBuilder.build();
            this.reloadJob(jobObject, JobLoadTriggerType.SERVER_RESTART);
        }

        @Override
        public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
            processTaskSerialNumberService.serialNumberSeedReset(YearMonthAndAutoIncrementPolicy.class.getName());
        }
    }
}
