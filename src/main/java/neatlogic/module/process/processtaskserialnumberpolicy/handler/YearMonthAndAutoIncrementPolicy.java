/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.processtaskserialnumberpolicy.handler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.module.process.service.ProcessTaskSerialNumberService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
        public void reloadJob(JobObject jobObject) {
            String tenantUuid = jobObject.getTenantUuid();
            TenantContext.get().switchTenant(tenantUuid);
            if (CronExpression.isValidExpression(cron)) {
                JobObject.Builder newJobObjectBuilder =
                        new JobObject.Builder(jobObject.getJobName(), this.getGroupName(), this.getClassName(),
                                TenantContext.get().getTenantUuid()).withCron(cron);
                JobObject newJobObject = newJobObjectBuilder.build();
                schedulerManager.loadJob(newJobObject);
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
            this.reloadJob(jobObject);
        }

        @Override
        public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
            processTaskSerialNumberService.serialNumberSeedReset(YearMonthAndAutoIncrementPolicy.class.getName());
        }
    }
}
