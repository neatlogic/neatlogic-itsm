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

package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.constvalue.automatic.FailPolicy;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepInOperationVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.automatic.AutomaticConfigVo;
import neatlogic.framework.process.dto.automatic.ProcessTaskStepAutomaticRequestVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.schedule.plugin.ProcessTaskAutomaticJob;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskAutomaticService;
import neatlogic.module.process.thread.ProcessTaskAutomaticThread;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AutomaticProcessComponent extends ProcessStepHandlerBase {
    private final Logger logger = LoggerFactory.getLogger(AutomaticProcessComponent.class);
    // 激活自动处理步骤时，如果在时间窗口内，在`processtask_step_in_operation`表中插入一条数据，标识该步骤正在后台处理中，有效时长3秒
    private final long EXPIRETIME = 3000;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;
    @Resource
    ProcessTaskAutomaticService processTaskAutomaticService;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.AUTOMATIC.getHandler();
    }

    @Override
    public String getType() {
        return ProcessStepHandlerType.AUTOMATIC.getType();
    }

    @Override
    public ProcessStepMode getMode() {
        return ProcessStepMode.MT;
    }

    @Override
    public JSONObject getChartConfig() {
        return new JSONObject() {
            {
                this.put("icon", "tsfont-duixiangcunchu");
                this.put("shape", "L-rectangle-50%:R-rectangle-50%");
                this.put("width", 68);
                this.put("height", 40);
            }
        };
    }

    @Override
    public String getName() {
        return ProcessStepHandlerType.AUTOMATIC.getName();
    }

    @Override
    public int getSort() {
        return 8;
    }

    @Override
    protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        try {
            AutomaticConfigVo automaticConfigVo = processTaskAutomaticService.getAutomaticConfigVoByProcessTaskStepId(currentProcessTaskStepVo.getId());
            JSONObject requestAudit = new JSONObject();
            requestAudit.put("integrationUuid", automaticConfigVo.getBaseIntegrationUuid());
            requestAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
            requestAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
            requestAudit.put("status", ProcessTaskStepStatus.getJson(ProcessTaskStepStatus.PENDING.getValue()));
            JSONObject baseSuccessConfig = automaticConfigVo.getBaseSuccessConfig();
            if (MapUtils.isNotEmpty(baseSuccessConfig)) {
                requestAudit.put("successConfig", baseSuccessConfig);
            } else {
                JSONObject successConfig = new JSONObject();
                successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
                requestAudit.put("successConfig", successConfig);
            }

            JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
            int isTimeToRun = 0;
            //检验执行时间窗口
            if (MapUtils.isNotEmpty(timeWindowConfig)) {
                String startTime = timeWindowConfig.getString("startTime");
                String endTime = timeWindowConfig.getString("endTime");
                if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                    isTimeToRun = TimeUtil.isInTimeWindow(startTime, endTime);
                }
            }
            if (isTimeToRun == 0) {
                requestAudit.put("startTime", System.currentTimeMillis());
            } else {
                requestAudit.put("startTime", TimeUtil.getDateByHourMinute(timeWindowConfig.getString("startTime"), isTimeToRun > 0 ? 1 : 0));
            }
            JSONObject data = new JSONObject();
            data.put("requestAudit", requestAudit);
            ProcessTaskStepDataVo auditDataVo = new ProcessTaskStepDataVo(
                    currentProcessTaskStepVo.getProcessTaskId(),
                    currentProcessTaskStepVo.getId(),
                    ProcessTaskStepDataType.AUTOMATIC.getValue(),
                    SystemUser.SYSTEM.getUserUuid()
            );
            auditDataVo.setData(data.toJSONString());
            processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
            UserContext.init(SystemUser.SYSTEM);
            if (Objects.equals(isTimeToRun, 0)) {
//            System.out.println("在时间窗口内，直接发送请求");
                IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                if (processStepInternalHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
                }
                ProcessTaskStepInOperationVo processTaskStepInOperationVo = new ProcessTaskStepInOperationVo(
                        currentProcessTaskStepVo.getProcessTaskId(),
                        currentProcessTaskStepVo.getId(),
                        "request",
                        new Date(System.currentTimeMillis() + EXPIRETIME)
                );
                // 后台异步操作步骤前，在`processtask_step_in_operation`表中插入一条数据，标识该步骤正在后台处理中，异步处理完删除
                processStepInternalHandler.insertProcessTaskStepInOperation(processTaskStepInOperationVo);
                TransactionSynchronizationPool.execute(new ProcessTaskAutomaticThread(currentProcessTaskStepVo, processTaskStepInOperationVo.getId()));
            } else {
//            System.out.println("不在时间窗口内，加载定时作业，定时发送请求");
                IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
                if (jobHandler == null) {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
                }
                ProcessTaskStepAutomaticRequestVo processTaskStepAutomaticRequestVo = new ProcessTaskStepAutomaticRequestVo();
                processTaskStepAutomaticRequestVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                processTaskStepAutomaticRequestVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                processTaskStepAutomaticRequestVo.setType("request");
                processTaskMapper.insertProcessTaskStepAutomaticRequest(processTaskStepAutomaticRequestVo);
                JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                        processTaskStepAutomaticRequestVo.getId().toString(),
                        jobHandler.getGroupName(),
                        jobHandler.getClassName(),
                        TenantContext.get().getTenantUuid()
                );
                JobObject jobObject = jobObjectBuilder.build();
                jobHandler.reloadJob(jobObject);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ProcessTaskException(e);
        }
        return 1;
    }

//    @Override
//    protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
//        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
//        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
//        //获取参数
//        JSONObject automaticConfig = null;
//        try {
//            JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
//            currentProcessTaskStepVo.getParamObj().putAll(stepConfigObj);
//            if (MapUtils.isNotEmpty(stepConfigObj)) {
//                automaticConfig = stepConfigObj.getJSONObject("automaticConfig");
//            }
//        } catch (Exception ex) {
//            logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
//        }
//        //初始化audit
//        AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
//        processTaskAutomaticService.initProcessTaskStepData(currentProcessTaskStepVo, automaticConfigVo, null, "request");
//        requestFirst(currentProcessTaskStepVo, automaticConfig);
//        return 0;
//    }
//
//
//    /*
//     * automatic 第一次请求
//     *
//     * @param automaticConfig
//     */
//    private void requestFirst(ProcessTaskStepVo currentProcessTaskStepVo, JSONObject automaticConfig) {
//        TransactionSynchronizationPool.execute(new RequestFirstThread(currentProcessTaskStepVo, automaticConfig));
//    }
//
//    private class RequestFirstThread extends NeatLogicThread {
//        private JSONObject automaticConfig;
//        private ProcessTaskStepVo currentProcessTaskStepVo;
//
//        private RequestFirstThread(ProcessTaskStepVo currentProcessTaskStepVo, JSONObject automaticConfig) {
//            super("REQUEST-FIRST");
//            this.automaticConfig = automaticConfig;
//            this.currentProcessTaskStepVo = currentProcessTaskStepVo;
//        }
//
//        @Override
//        protected void execute() {
//            UserContext.init(SystemUser.SYSTEM.getUserVo(), SystemUser.SYSTEM.getTimezone());
//            AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
//            JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
//            automaticConfigVo.setIsRequest(true);
//            Integer isTimeToRun = null;
//            //检验执行时间窗口
//            if (timeWindowConfig != null) {
//                isTimeToRun = TimeUtil.isInTimeWindow(timeWindowConfig.getString("startTime"), timeWindowConfig.getString("endTime"));
//            }
//            if (timeWindowConfig == null || isTimeToRun == 0) {
//                processTaskAutomaticService.runRequest(automaticConfigVo, currentProcessTaskStepVo);
//            } else {//loadJob,定时执行第一次请求
//                //初始化audit执行状态
//                JSONObject audit = null;
//                ProcessTaskStepDataVo data = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), ProcessTaskStepDataType.AUTOMATIC.getValue(), SystemUser.SYSTEM.getUserId()));
//                JSONObject dataObject = data.getData();
//                audit = dataObject.getJSONObject("requestAudit");
//                audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
//                processTaskAutomaticService.initJob(automaticConfigVo, currentProcessTaskStepVo, dataObject);
//                data.setData(dataObject.toJSONString());
//                processTaskStepDataMapper.replaceProcessTaskStepData(data);
//            }
//        }
//
//    }


    @Override
    protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
        return 1;
    }

    @Override
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
        return defaultAssign(currentProcessTaskStepVo, workerSet);
    }

    @Override
    protected Set<Long> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo, List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
        return defaultGetNext(nextStepIdList, nextStepId);
    }

    @Override
    protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myStart(ProcessTaskStepVo processTaskStepVo) {
        return 0;
    }

    @Override
    public Boolean isAllowStart() {
        return null;
    }

    @Override
    protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                .getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),
                        currentProcessTaskStepVo.getId(), currentProcessTaskStepVo.getHandler(), SystemUser.SYSTEM.getUserUuid()));
        if (stepDataVo != null) {
            currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.AUTOMATICINFO.getParamName(), stepDataVo.getData());
        }
        processTaskMapper.deleteProcessTaskStepAutomaticRequestByProcessTaskStepId(currentProcessTaskStepVo.getId());
        return 0;
    }

    @Override
    protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        if(StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
            currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(), currentProcessTaskStepVo.getError());
        }
        /* 处理历史记录 **/
        String action = currentProcessTaskStepVo.getParamObj().getString("action");
        processStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.getProcessTaskAuditType(action));
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
        return 1;
    }

    @Override
    protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 1;
    }

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
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
