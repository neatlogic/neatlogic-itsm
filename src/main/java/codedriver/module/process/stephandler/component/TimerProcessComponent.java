/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.stephandler.component;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepTimerVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.module.process.schedule.plugin.ProcessTaskStepTimerCompleteJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author linbq
 * @since 2021/12/27 16:03
 **/
@Component
public class TimerProcessComponent extends ProcessStepHandlerBase {
    @Override
    public String getHandler() {
        return ProcessStepHandlerType.TIMER.getHandler();
    }

    @Override
    public JSONObject getChartConfig() {
        return new JSONObject() {
            {
                this.put("icon", "tsfont-timer");
                this.put("shape", "L-rectangle:R-rectangle");
                this.put("width", 68);
                this.put("height", 40);
            }
        };
    }

    @Override
    public String getType() {
        return ProcessStepHandlerType.TIMER.getType();
    }

    @Override
    public ProcessStepMode getMode() {
        return ProcessStepMode.MT;
    }

    @Override
    public String getName() {
        return ProcessStepHandlerType.TIMER.getName();
    }

    @Override
    public int getSort() {
        return 11;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Boolean isAllowStart() {
        return false;
    }

    @Override
    protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        String configHash = currentProcessTaskStepVo.getConfigHash();
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(configHash);
//        {
//            "type": "custom/form",
//                "value":"",
//                "attributeUuid": "",
//        }
        if (StringUtils.isNotBlank(stepConfig)) {
            JSONObject config = JSONObject.parseObject(stepConfig);
            if (MapUtils.isNotEmpty(config)) {
                Date triggerTime = null;
                String type = config.getString("type");
                if ("custom".equals(type)) {
                    String value = config.getString("value");
                    if (StringUtils.isNotBlank(value)) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        LocalTime localTime = LocalTime.from(dateTimeFormatter.parse(value));
                        LocalDateTime localDateTime = localTime.atDate(LocalDate.now());
                        triggerTime = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));
                    }
                } else if ("form".equals(type)) {
                    String attributeUuid = config.getString("attributeUuid");
                    if (StringUtils.isNotBlank(attributeUuid)) {
                        ProcessTaskFormAttributeDataVo searchVo = new ProcessTaskFormAttributeDataVo();
                        searchVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        searchVo.setAttributeUuid(attributeUuid);
                        ProcessTaskFormAttributeDataVo dataVo = processTaskMapper.getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(searchVo);
                        if (dataVo != null) {
                            String value = dataVo.getData();
                            if (StringUtils.isNotBlank(value)) {
                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                LocalDateTime localDateTime = LocalDateTime.from(dateTimeFormatter.parse(value));
                                triggerTime = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));
                            }
                        }
                    }
                }
                if (triggerTime != null) {
                    if (triggerTime.after(new Date())) {
                        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskStepTimerCompleteJob.class.getName());
                        if (jobHandler == null) {
                            throw new ScheduleHandlerNotFoundException(ProcessTaskStepTimerCompleteJob.class.getName());
                        }
                        ProcessTaskStepTimerVo processTaskStepTimerVo = new ProcessTaskStepTimerVo();
                        processTaskStepTimerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        processTaskStepTimerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                        processTaskStepTimerVo.setTriggerTime(triggerTime);
                        processTaskMapper.insertProcessTaskStepTimer(processTaskStepTimerVo);
                        JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                                processTaskStepTimerVo.getId().toString(),
                                jobHandler.getGroupName(),
                                jobHandler.getClassName(),
                                TenantContext.get().getTenantUuid()
                        );
                        JobObject jobObject = jobObjectBuilder.build();
                        jobHandler.reloadJob(jobObject);
                    }
                }
            }
        }
        return 1;
    }

    @Override
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
        return defaultAssign(currentProcessTaskStepVo, workerSet);
    }

    @Override
    protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myStart(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myReapproval(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myReapprovalAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected Set<Long> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo, List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
        return defaultGetNext(nextStepIdList, nextStepId);
    }

    @Override
    protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    /**
     * 正向输出路径数量
     * -1代表不限制
     * @return
     */
    @Override
    public int getForwardOutnputQuantity() {
        return 1;
    }
    /**
     * 回退输入路径数量
     * -1代表不限制
     * @return
     */
    @Override
    public  int getBackwardInputQuantity() {
        return 0;
    }
    /**
     * 回退输出路径数量
     * -1代表不限制
     * @return
     */
    @Override
    public  int getBackwardOutputQuantity() {
        return 0;
    }
}
