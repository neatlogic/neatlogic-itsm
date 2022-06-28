/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.processtaskserialnumberpolicy.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.process.service.ProcessTaskSerialnumberService;
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
import java.util.List;

@Service
public class YearMonthAndAutoIncrementPolicy implements IProcessTaskSerialNumberPolicyHandler {
    private Logger logger = LoggerFactory.getLogger(YearMonthAndAutoIncrementPolicy.class);

    @Resource
    private ProcessTaskSerialnumberService processTaskSerialnumberService;

    @Override
    public String getName() {
        return "年月 + 自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
        return processTaskSerialnumberService.makeupFormAttributeList(10, 16);
    }

    @Override
    public JSONObject makeupConfig(JSONObject jsonObj) {
        JSONObject resultObj = new JSONObject();
        Long startValue = jsonObj.getLong("startValue");
        if (startValue == null) {
            startValue = 0L;
        }
        resultObj.put("startValue", startValue);
        Integer digits = jsonObj.getInteger("digits");
        if (digits != null) {
            resultObj.put("digits", digits);
            resultObj.put("numberOfDigits", digits - 6);
        }
        return resultObj;
    }

    @Override
    public String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialnumberService.genarate(processTaskSerialNumberPolicyVo, new SimpleDateFormat("yyyyMM"));
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialnumberService.batchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, new SimpleDateFormat("yyyyMM"));
    }


    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialnumberService.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, true);
    }

    @Component
    @DisallowConcurrentExecution
    private static class ProcessTaskSerialNumberSeedResetJob extends JobBase {

        private String cron = "0 0 0 * * ?";

        @Autowired
        private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

        @Override
        public String getGroupName() {
            return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SERIALNUMBERSEED-" + this.getClass().getSimpleName() + "-RESET";
        }

        @Override
        public Boolean isHealthy(JobObject jobObject) {
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
            String handler = YearMonthAndAutoIncrementPolicy.class.getName();
            List<ProcessTaskSerialNumberPolicyVo> processTaskSerialNumberPolicyList =
                    processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyListByHandler(handler);
            for (ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo : processTaskSerialNumberPolicyList) {
                ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicy =
                        processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(
                                processTaskSerialNumberPolicyVo.getChannelTypeUuid());
                Long startValue = 1L;
                Long value = processTaskSerialNumberPolicy.getConfig().getLong("startValue");
                if (value != null) {
                    startValue = value;
                }
                processTaskSerialNumberPolicyVo.setSerialNumberSeed(startValue);
                processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(
                        processTaskSerialNumberPolicyVo.getChannelTypeUuid(), startValue);
            }
        }
    }
}
