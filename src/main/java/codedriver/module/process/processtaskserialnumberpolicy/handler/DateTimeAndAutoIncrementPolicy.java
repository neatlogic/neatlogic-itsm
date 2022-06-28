/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.processtaskserialnumberpolicy.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
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
import java.util.*;

@Service
public class DateTimeAndAutoIncrementPolicy implements IProcessTaskSerialNumberPolicyHandler {
    private Logger logger = LoggerFactory.getLogger(DateTimeAndAutoIncrementPolicy.class);
    @Resource
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Override
    public String getName() {
        return "年月日 + 自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
        JSONArray resultArray = new JSONArray();
        {
            /** 起始值 **/
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", "text");
            jsonObj.put("name", "startValue");
            jsonObj.put("value", "");
            jsonObj.put("defaultValue", 1);
            jsonObj.put("width", 200);
            jsonObj.put("maxlength", 5);
            jsonObj.put("label", "起始位");
            jsonObj.put("validateList", Arrays.asList("required", new JSONObject() {
                {
                    this.put("name", "integer_p");
                    this.put("message", "请输入正整数");
                }
            }));
            jsonObj.put("placeholder", "1-99999");
            resultArray.add(jsonObj);
        }
        {
            /** 位数 **/
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", "select");
            jsonObj.put("name", "digits");
            jsonObj.put("value", "");
            jsonObj.put("defaultValue", "");
            jsonObj.put("width", 200);
            jsonObj.put("label", "工单号位数");
            jsonObj.put("maxlength", 5);
            jsonObj.put("validateList", Arrays.asList("required"));
            jsonObj.put("dataList", new ArrayList<ValueTextVo>() {
                {
                    this.add(new ValueTextVo(10, "10"));
                    this.add(new ValueTextVo(11, "11"));
                    this.add(new ValueTextVo(12, "12"));
                    this.add(new ValueTextVo(13, "13"));
                    this.add(new ValueTextVo(14, "14"));
                    this.add(new ValueTextVo(15, "15"));
                    this.add(new ValueTextVo(16, "16"));
                }
            });
            resultArray.add(jsonObj);
        }
        return resultArray;
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
            resultObj.put("numberOfDigits", digits - 8);
        }
        return resultObj;
    }

    @Override
    public String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long serialNumberSeed = processTaskSerialNumberPolicyVo.getSerialNumberSeed();
        if (serialNumberSeed > max) {
            serialNumberSeed -= max;
        }
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(
                processTaskSerialNumberPolicyVo.getChannelTypeUuid(), serialNumberSeed + 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
        return channelTypeVo.getPrefix() + sdf.format(new Date()) + String.format("%0" + numberOfDigits + "d", serialNumberSeed);
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        try {
            ProcessTaskVo processTaskVo = new ProcessTaskVo();
            processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
            ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
            if (channelTypeVo == null) {
                throw new ChannelTypeNotFoundException(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
            }
            int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
            if (rowNum > 0) {
                int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
                long max = (long) Math.pow(10, numberOfDigits) - 1;
                long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
                long serialNumberSeed = startValue;
                String timeFormat = null;
                processTaskVo.setPageSize(100);
                int pageCount = PageUtil.getPageCount(rowNum, processTaskVo.getPageSize());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    processTaskVo.setCurrentPage(currentPage);
                    List<ProcessTaskVo> processTaskList =
                            processTaskMapper.getProcessTaskListByChannelTypeUuidAndStartTime(processTaskVo);
                    for (ProcessTaskVo processTask : processTaskList) {
                        String startTimeFormat = sdf.format(processTask.getStartTime());
                        if (!Objects.equals(timeFormat, startTimeFormat)) {
                            serialNumberSeed = startValue;
                            timeFormat = startTimeFormat;
                        }
                        String serialNumber = channelTypeVo.getPrefix() + startTimeFormat + String.format("%0" + numberOfDigits + "d", serialNumberSeed);
                        processTaskMapper.updateProcessTaskSerialNumberById(processTask.getId(), serialNumber);
                        processTaskSerialNumberMapper.insertProcessTaskSerialNumber(processTask.getId(), serialNumber);
                        serialNumberSeed++;
                        if (serialNumberSeed > max) {
                            serialNumberSeed -= max;
                        }
                    }
                }
            }
            return rowNum;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyEndTimeByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
//        System.out.println("------------");
        return 0;
    }


    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        processTaskVo.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
        rowNum += startValue;
        return rowNum % max;
    }

    @Component
    @DisallowConcurrentExecution
    private static class ProcessTaskSerialNumberSeedResetJob extends JobBase {

        private String cron = "0 0 0 * * ?";

        @Autowired
        private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

        @Override
        public String getGroupName() {
            return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SERIALNUMBERSEED-RESET";
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
            String handler = DateTimeAndAutoIncrementPolicy.class.getName();
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
