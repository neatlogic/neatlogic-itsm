/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.*;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.schedule.plugin.ProcessTaskStepTimerCompleteJob;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author linbq
 * @since 2021/12/27 16:03
 **/
@Component
public class TimerProcessComponent extends ProcessStepHandlerBase {

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private SchedulerManager schedulerManager;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    private final Logger logger = LoggerFactory.getLogger(TimerProcessComponent.class);

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
        try {
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
                if ("custom".equals(type)) {//自定义类型没有使用
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
                        ProcessTaskFormAttributeDataVo dataVo = processTaskService.getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(currentProcessTaskStepVo.getProcessTaskId(), attributeUuid);
                        if (dataVo != null) {
                            String value = dataVo.getData();
                            if (StringUtils.isNotBlank(value)) {
                                String format = "yyyy-MM-dd HH:mm";
                                ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                                if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
                                    String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                                    if (StringUtils.isNotBlank(formContent)) {
                                        FormVersionVo fromFormVersion = new FormVersionVo();
                                        fromFormVersion.setFormConfig(JSONObject.parseObject(formContent));
                                        String mainSceneUuid = fromFormVersion.getFormConfig().getString("uuid");
                                        fromFormVersion.setSceneUuid(mainSceneUuid);
                                        List<FormAttributeVo> fromFormAttributeList = fromFormVersion.getFormAttributeList();
                                        for (FormAttributeVo formAttributeVo : fromFormAttributeList) {
                                            if (Objects.equals(formAttributeVo.getUuid(), attributeUuid)) {
                                                JSONObject configObj = formAttributeVo.getConfig();
                                                if (MapUtils.isNotEmpty(configObj)) {
                                                    IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                                                    if (handler != null) {
                                                        JSONObject detailedData = handler.getDetailedData(dataVo, configObj);
                                                        format = detailedData.getString("format");
                                                    }
                                                    // 当styleType是"/"，showType="yyyy-MM-dd"，保存的日期值是2022/02/11，需要将yyyy-MM-dd转换成yyyy/MM/dd
//                                                    String showType = configObj.getString("showType");
//                                                    String styleType = configObj.getString("styleType");
//                                                    if ("-".equals(styleType)) {
//                                                        pattern = showType;
//                                                    } else {
//                                                        char oldChar = '-';
//                                                        char newChar = styleType.charAt(0);
//                                                        pattern = showType.replace(oldChar, newChar);
//                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                SimpleDateFormat sdf = new SimpleDateFormat(format);
                                try {
                                    triggerTime = sdf.parse(value);
                                } catch (ParseException e) {
                                    logger.error(e.getMessage(), e);
                                }
//                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//                                LocalDateTime localDateTime = LocalDateTime.from(dateTimeFormatter.parse(value));
//                                triggerTime = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));
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
                                currentProcessTaskStepVo.getId().toString(),
                                jobHandler.getGroupName(),
                                jobHandler.getClassName(),
                                TenantContext.get().getTenantUuid()
                        );
                        JobObject jobObject = jobObjectBuilder.build();
                        jobHandler.reloadJob(jobObject);
                    } else {
                        ProcessTaskStepInOperationVo processTaskStepInOperationVo = new ProcessTaskStepInOperationVo(
                                currentProcessTaskStepVo.getProcessTaskId(),
                                currentProcessTaskStepVo.getId(),
                                ProcessTaskOperationType.STEP_COMPLETE.getValue()
                        );
                        IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                        if (processStepInternalHandler == null) {
                            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
                        }
                        /** 后台异步操作步骤前，在`processtask_step_in_operation`表中插入一条数据，标识该步骤正在后台处理中，异步处理完删除 **/
                        processStepInternalHandler.insertProcessTaskStepInOperation(processTaskStepInOperationVo);
                        ProcessStepThread thread = new ProcessStepThread(currentProcessTaskStepVo) {
                            @Override
                            public void myExecute() {
                                UserContext.init(SystemUser.SYSTEM);
                                IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                                handler.autoComplete(currentProcessTaskStepVo);
                            }
                        };
                        thread.setSupplier(() -> processTaskMapper.deleteProcessTaskStepInOperationById(processTaskStepInOperationVo.getId()));
                        TransactionSynchronizationPool.execute(thread);
                    }
                } else {
                    IProcessStepHandler processStepHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                    if (processStepHandler != null) {
                        try {
                            processStepHandler.assign(currentProcessTaskStepVo);
                        } catch (ProcessTaskException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return 1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ProcessTaskException(e.getMessage());
        }
    }

    @Override
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
        return defaultAssign(currentProcessTaskStepVo, workerSet);
    }

    @Override
    protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepTimerVo processTaskStepTimerVo = processTaskMapper.getProcessTaskStepTimerByProcessTaskStepId(currentProcessTaskStepVo.getId());
        if (processTaskStepTimerVo != null) {
            IJob jobHandler = SchedulerManager.getHandler(ProcessTaskStepTimerCompleteJob.class.getName());
            if (jobHandler == null) {
                throw new ScheduleHandlerNotFoundException(ProcessTaskStepTimerCompleteJob.class.getName());
            }
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    currentProcessTaskStepVo.getId().toString(),
                    jobHandler.getGroupName(),
                    jobHandler.getClassName(),
                    TenantContext.get().getTenantUuid()
            );
            JobObject jobObject = jobObjectBuilder.build();
            schedulerManager.unloadJob(jobObject);
            processTaskMapper.deleteProcessTaskStepTimerByProcessTaskStepId(currentProcessTaskStepVo.getId());
        }
        return 1;
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
        processTaskMapper.deleteProcessTaskStepTimerByProcessTaskStepId(currentProcessTaskStepVo.getId());
        return 1;
    }

    @Override
    protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        if (StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
            currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(), currentProcessTaskStepVo.getError());
        }
        /** 处理历史记录 **/
        processStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.COMPLETE);
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
     *
     * @return
     */
    @Override
    public int getForwardOutputQuantity() {
        return 1;
    }

    /**
     * 回退输入路径数量
     * -1代表不限制
     *
     * @return
     */
    @Override
    public int getBackwardInputQuantity() {
        return 0;
    }

    /**
     * 回退输出路径数量
     * -1代表不限制
     *
     * @return
     */
    @Override
    public int getBackwardOutputQuantity() {
        return 0;
    }

    @Override
    public boolean disableAssign() {
        return true;
    }

    @Override
    public boolean allowDispatchStepWorker() {
        return false;
    }
}
