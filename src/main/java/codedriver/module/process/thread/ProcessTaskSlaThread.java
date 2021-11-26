/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.ConditionParamContext;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dto.condition.ConditionConfigVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.SlaStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import codedriver.framework.process.sla.core.ISlaCalculateHandler;
import codedriver.framework.process.sla.core.SlaCalculateHandlerFactory;
import codedriver.framework.process.dto.*;
import codedriver.module.process.notify.schedule.plugin.ProcessTaskSlaNotifyJob;
import codedriver.module.process.notify.schedule.plugin.ProcessTaskSlaTransferJob;
import codedriver.framework.util.WorkTimeUtil;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.transaction.util.TransactionUtil;
import codedriver.framework.util.RunScriptUtil;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.module.process.sla.handler.DefaultSlaCalculateHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ProcessTaskSlaThread extends CodeDriverThread {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskSlaThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static TransactionUtil transactionUtil;
    private static ProcessTaskService processTaskService;

    @Resource
    public void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
    }

    @Resource
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Resource
    public void setTransactionUtil(TransactionUtil _transactionUtil) {
        transactionUtil = _transactionUtil;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private ProcessTaskVo currentProcessTaskVo;

    public ProcessTaskSlaThread() {
        super("PROCESSTASK-SLA");
    }

    public ProcessTaskSlaThread(ProcessTaskVo _currentProcessTaskVo, ProcessTaskStepVo _currentProcessTaskStepVo) {
        super("PROCESSTASK-SLA" + (_currentProcessTaskStepVo != null ? "-" + _currentProcessTaskStepVo.getId() : "")
                + (_currentProcessTaskVo != null ? "-" + _currentProcessTaskVo.getId() : ""));
        currentProcessTaskVo = _currentProcessTaskVo;
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
    }

    /**
     * 转换成毫秒单位值
     *
     * @param time 时长
     * @param unit 单位
     * @return
     */
    private static long toMillis(int time, String unit) {
        if ("hour".equals(unit)) {
            return (long) time * 60 * 60 * 1000;
        } else if ("day".equals(unit)) {
            return (long) time * 24 * 60 * 60 * 1000;
        } else {
            return (long) time * 60 * 1000;
        }
    }

    /**
     * 根据时效配置信息和工单详情，计算生效的时效时长
     *
     * @param slaConfigObj  时效配置信息
     * @param processTaskVo 工单详情
     * @return
     */
    private Long getSlaTimeSumBySlaConfig(JSONObject slaConfigObj, ProcessTaskVo processTaskVo) {
        JSONArray policyList = slaConfigObj.getJSONArray("calculatePolicyList");
        if (CollectionUtils.isNotEmpty(policyList)) {
            for (int i = 0; i < policyList.size(); i++) {
                JSONObject policyObj = policyList.getJSONObject(i);
                int enablePriority = policyObj.getIntValue("enablePriority");
                int time = policyObj.getIntValue("time");
                String unit = policyObj.getString("unit");
                JSONArray priorityList = policyObj.getJSONArray("priorityList");
                JSONArray conditionGroupList = policyObj.getJSONArray("conditionGroupList");
                /* 如果没有规则，则默认生效，如果有规则，以规则计算结果判断是否生效 **/
                boolean isHit = true;
                if (CollectionUtils.isNotEmpty(conditionGroupList)) {
                    try {
                        JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
                        ConditionParamContext.init(conditionParamData);
                        ConditionConfigVo conditionConfigVo = new ConditionConfigVo(policyObj);
                        String script = conditionConfigVo.buildScript();
                        // ((false || true) || (true && false) || (true || false))
                        isHit = RunScriptUtil.runScript(script);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        ConditionParamContext.get().release();
                    }
                }
                if (isHit) {
                    if (enablePriority == 0) {
                        return toMillis(time, unit);
                    } else {// 关联优先级
                        if (CollectionUtils.isNotEmpty(priorityList)) {
                            for (int p = 0; p < priorityList.size(); p++) {
                                JSONObject priorityObj = priorityList.getJSONObject(p);
                                if (priorityObj.getString("priorityUuid").equals(processTaskVo.getPriorityUuid())) {
                                    return toMillis(priorityObj.getIntValue("time"),
                                            priorityObj.getString("unit"));
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 计算realTimeLeft（直接剩余时间）、timeLeft(工作日历剩余时间)、realExpireTime（直接超时时间点）、expireTime(工作日历超时时间点)
     *
     * @param slaId        时效id
     * @param timeSum      时效生效时长
     * @param worktimeUuid 工作时间窗口uuid
     * @return 时效信息
     */
    private ProcessTaskSlaTimeVo getSlaTime(Long slaId, Long timeSum, long currentTimeMillis, String worktimeUuid, ProcessTaskSlaTimeCostVo timeCostVo) {
        ProcessTaskSlaTimeVo slaTimeVo = new ProcessTaskSlaTimeVo();
        slaTimeVo.setTimeSum(timeSum);
        slaTimeVo.setSlaId(slaId);
        /** 非第一次进入，进行时间扣减 **/
        long realTimeLeft = timeSum - timeCostVo.getRealTimeCost();
        long timeLeft = timeSum - timeCostVo.getTimeCost();
        slaTimeVo.setRealTimeLeft(realTimeLeft);
        slaTimeVo.setTimeLeft(timeLeft);

        slaTimeVo.setRealExpireTime(new Date(currentTimeMillis + realTimeLeft));
        if (StringUtils.isNotBlank(worktimeUuid)) {
            if (timeLeft > 0) {
                long expireTime = WorkTimeUtil.calculateExpireTime(currentTimeMillis, timeLeft, worktimeUuid);
                slaTimeVo.setExpireTime(new Date(expireTime));
            } else {
                long expireTime = WorkTimeUtil.calculateExpireTimeForTimedOut(currentTimeMillis, -timeLeft, worktimeUuid);
                slaTimeVo.setExpireTime(new Date(expireTime));
            }
        } else {
            slaTimeVo.setExpireTime(new Date(currentTimeMillis + timeLeft));
        }
        return slaTimeVo;
    }

    private void adjustJob(ProcessTaskSlaTimeVo slaTimeVo, JSONObject slaConfigObj, Date oldExpireTime) {
        /* 有超时时间点 **/
        if (slaTimeVo.getExpireTime() != null) {
            /* 是否需要启动作业 **/
            boolean isStartJob = false;
            List<Long> processTaskStepIdList =
                    processTaskMapper.getProcessTaskStepIdListBySlaId(slaTimeVo.getSlaId());
            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                List<ProcessTaskStepVo> processTaskStepList =
                        processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                        if (ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())
                                || ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                            isStartJob = true;
                            break;
                        }
                    }
                }
            }
            /* 作业是否已启动 **/
            boolean jobStarted = false;
            List<ProcessTaskSlaNotifyVo> processTaskSlaNotifyList =
                    processTaskMapper.getProcessTaskSlaNotifyBySlaId(slaTimeVo.getSlaId());
            List<ProcessTaskSlaTransferVo> processTaskSlaTransferList =
                    processTaskMapper.getProcessTaskSlaTransferBySlaId(slaTimeVo.getSlaId());
            if (CollectionUtils.isNotEmpty(processTaskSlaNotifyList)
                    || CollectionUtils.isNotEmpty(processTaskSlaTransferList)) {
                jobStarted = true;
            }
            if (jobStarted) {
                if (!isStartJob || !slaTimeVo.getExpireTime().equals(oldExpireTime)) {
                    processTaskMapper.deleteProcessTaskSlaTransferBySlaId(slaTimeVo.getSlaId());
                    processTaskMapper.deleteProcessTaskSlaNotifyBySlaId(slaTimeVo.getSlaId());
                    jobStarted = false;
                }
            }
            /* 作业需要启动，且未启动时，加载定时作业 **/
            if (isStartJob && !jobStarted) {
                // 加载定时作业，执行超时通知操作
                JSONArray notifyPolicyList = slaConfigObj.getJSONArray("notifyPolicyList");
                if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
                    for (int i = 0; i < notifyPolicyList.size(); i++) {
                        JSONObject notifyPolicyObj = notifyPolicyList.getJSONObject(i);
                        ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = new ProcessTaskSlaNotifyVo();
                        processTaskSlaNotifyVo.setSlaId(slaTimeVo.getSlaId());
                        processTaskSlaNotifyVo.setConfig(notifyPolicyObj.toJSONString());
                        // 需要发通知时写入数据，执行完毕后清除
                        processTaskMapper.insertProcessTaskSlaNotify(processTaskSlaNotifyVo);
                        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskSlaNotifyJob.class.getName());
                        if (jobHandler != null) {
                            JobObject.Builder jobObjectBuilder =
                                    new JobObject.Builder(processTaskSlaNotifyVo.getId().toString(),
                                            jobHandler.getGroupName(), jobHandler.getClassName(),
                                            TenantContext.get().getTenantUuid()).addData("slaNotifyId",
                                            processTaskSlaNotifyVo.getId());
                            JobObject jobObject = jobObjectBuilder.build();
                            jobHandler.reloadJob(jobObject);
                        } else {
                            throw new ScheduleHandlerNotFoundException(ProcessTaskSlaNotifyJob.class.getName());
                        }
                    }
                }
                // 加载定时作业，执行超时转交操作
                JSONArray transferPolicyList = slaConfigObj.getJSONArray("transferPolicyList");
                if (CollectionUtils.isNotEmpty(transferPolicyList)) {
                    for (int i = 0; i < transferPolicyList.size(); i++) {
                        JSONObject transferPolicyObj = transferPolicyList.getJSONObject(i);
                        ProcessTaskSlaTransferVo processTaskSlaTransferVo = new ProcessTaskSlaTransferVo();
                        processTaskSlaTransferVo.setSlaId(slaTimeVo.getSlaId());
                        processTaskSlaTransferVo.setConfig(transferPolicyObj.toJSONString());
                        // 需要转交时写入数据，执行完毕后清除
                        processTaskMapper.insertProcessTaskSlaTransfer(processTaskSlaTransferVo);
                        IJob jobHandler = SchedulerManager.getHandler(ProcessTaskSlaTransferJob.class.getName());
                        if (jobHandler != null) {
                            JobObject.Builder jobObjectBuilder =
                                    new JobObject.Builder(
                                            processTaskSlaTransferVo.getId().toString(),
                                            jobHandler.getGroupName(),
                                            jobHandler.getClassName(),
                                            TenantContext.get().getTenantUuid()
                                    ).addData("slaTransferId", processTaskSlaTransferVo.getId());
                            JobObject jobObject = jobObjectBuilder.build();
                            jobHandler.reloadJob(jobObject);
                        } else {
                            throw new ScheduleHandlerNotFoundException(ProcessTaskSlaTransferVo.class.getName());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        // 开始事务
        TransactionStatus transactionStatus = transactionUtil.openTx();
        try {
            Long processTaskId = null;
            List<Long> allSlaIdList = null;
            if (currentProcessTaskStepVo != null) {
//                System.out.println("processTaskStepId=" + currentProcessTaskStepVo.getId());
                allSlaIdList = processTaskMapper.getSlaIdListByProcessTaskStepId(currentProcessTaskStepVo.getId());
                processTaskId = currentProcessTaskStepVo.getProcessTaskId();
            } else if (currentProcessTaskVo != null) {
//                System.out.println("processTaskId=" + currentProcessTaskVo.getId());
                allSlaIdList = processTaskMapper.getSlaIdListByProcessTaskId(currentProcessTaskVo.getId());
                processTaskId = currentProcessTaskVo.getId();
            }
//            System.out.println("************************start*****************************************");
//            System.out.println(allSlaIdList);
            List<Long> slaIdList = new ArrayList<>();
            /** 遍历删除不需要计算的slaId **/
            for (Long slaId : allSlaIdList) {
                String config = processTaskMapper.getProcessTaskSlaConfigById(slaId);
                String calculateHandler = (String) JSONPath.read(config, "calculateHandler");
                if (StringUtils.isBlank(calculateHandler)) {
                    calculateHandler = DefaultSlaCalculateHandler.class.getSimpleName();
                }
                ISlaCalculateHandler handler = SlaCalculateHandlerFactory.getHandler(calculateHandler);
                if (handler == null) {
                    throw new SlaCalculateHandlerNotFoundException(calculateHandler);
                }
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
                SlaStatus status = handler.getStatus(processTaskStepList);
                ProcessTaskSlaTimeVo slaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
//                System.out.println("slaId=" + slaId);
//                String name = (String) JSONPath.read(config, "name");
//                System.out.println("name=" + name);
//                System.out.println("calculateHandler=" + calculateHandler);
//                System.out.println("status=" + status);
//                if (slaTimeVo == null) {
//                    System.out.println("slaTimeVo.status=null");
//                } else {
//                    System.out.println("slaTimeVo.status=" + slaTimeVo.getStatus());
//                }
                if (slaTimeVo == null) {
                    /** 当SLA未激活时，判断现在是否满足激活条件，不满足条件就删除 **/
                    if (SlaStatus.DOING == status) {
                        slaIdList.add(slaId);
                    }
                } else if (SlaStatus.DOING.name().toLowerCase().equals(slaTimeVo.getStatus())) {
                    slaIdList.add(slaId);
                } else if (SlaStatus.DONE.name().toLowerCase().equals(slaTimeVo.getStatus())) {
                    /** 当SLA已完成时，判断现在是否满足激活条件，不满足条件就删除 **/
                    if (status != null && SlaStatus.DONE != status) {
                        slaIdList.add(slaId);
                    } else if (handler.needDelete(processTaskStepList)) {
                        deleteSlaById(slaId);
                    }
                } else if (SlaStatus.PAUSE.name().toLowerCase().equals(slaTimeVo.getStatus())) {
                    /** 当SLA已暂停时，判断现在是否满足激活条件，不满足条件就删除 **/
                    if (SlaStatus.PAUSE != status) {
                        slaIdList.add(slaId);
                    } else if (handler.needDelete(processTaskStepList)) {
                        deleteSlaById(slaId);
                    }
                }
//                System.out.println("-------------------------------------------");
            }
//            System.out.println(slaIdList);
//            System.out.println("************************end*****************************************");
            if (CollectionUtils.isNotEmpty(slaIdList)) {
                ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
                processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
                processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
                String worktimeUuid = processTaskVo.getWorktimeUuid();
                for (Long slaId : slaIdList) {
                    processTaskMapper.getProcessTaskSlaLockById(slaId);
                    String config = processTaskMapper.getProcessTaskSlaConfigById(slaId);
                    JSONObject slaConfigObj = JSON.parseObject(config);
                    if (MapUtils.isNotEmpty(slaConfigObj)) {
                        /* 旧的超时时间点 **/
                        Date oldExpireTime = null;
                        Long oldTimeSum = null;
                        String oldSlaStatus = null;
                        boolean isSlaTimeExists = false;
                        /* 如果没有超时时间，证明第一次进入SLA标签范围，开始计算超时时间 **/
                        ProcessTaskSlaTimeVo oldSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
                        if (oldSlaTimeVo != null) {
                            /* 记录旧的超时时间点 **/
                            oldExpireTime = oldSlaTimeVo.getExpireTime();
                            oldTimeSum = oldSlaTimeVo.getTimeSum();
                            oldSlaStatus = oldSlaTimeVo.getStatus();
                            isSlaTimeExists = true;
                        }
                        Long timeSum = getSlaTimeSumBySlaConfig(slaConfigObj, processTaskVo);

                        // 修正最终超时日期
                        if (timeSum != null) {
                            String calculateHandler = slaConfigObj.getString("calculateHandler");
                            if (StringUtils.isBlank(calculateHandler)) {
                                calculateHandler = DefaultSlaCalculateHandler.class.getSimpleName();
                            }
                            ISlaCalculateHandler handler = SlaCalculateHandlerFactory.getHandler(calculateHandler);
                            if (handler == null) {
                                throw new SlaCalculateHandlerNotFoundException(calculateHandler);
                            }
                            long currentTimeMillis = System.currentTimeMillis();
                            ProcessTaskSlaTimeCostVo timeCostVo = handler.calculateTimeCost(slaId, currentTimeMillis, worktimeUuid);
                            ProcessTaskSlaTimeVo slaTimeVo = getSlaTime(slaId, timeSum, currentTimeMillis, worktimeUuid, timeCostVo);
                            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
                            SlaStatus status = handler.getStatus(processTaskStepList);
                            if (status != null) {
                                slaTimeVo.setStatus(status.name().toLowerCase());
//                                System.out.println("status=" + status.name().toLowerCase());
                            } else {
                                slaTimeVo.setStatus(oldSlaStatus);
//                                System.out.println("status=null");
                            }
//                            System.out.println("slaTimeVo=" + slaTimeVo);
                            if (Objects.equals(timeSum, oldTimeSum)) {
                                long expireTimeLong = 0L;
                                long oldExpireTimeLong = 0L;
                                if (slaTimeVo.getExpireTime() != null) {
                                    expireTimeLong = slaTimeVo.getExpireTime().getTime();
                                }
                                if (oldExpireTime != null) {
                                    oldExpireTimeLong = oldExpireTime.getTime();
                                }
                                /* 由于Date类型数据保存到MySql数据库时会丢失毫秒数值，只保留到秒的精度，所以两次计算超时时间点的差值小于1000时，说明时效没有被条件改变，不用更新 **/
                                if (expireTimeLong - oldExpireTimeLong < 1000) {
                                    if (!Objects.equals(slaTimeVo.getStatus(), oldSlaStatus)) {
                                        processTaskMapper.updateProcessTaskSlaTimeStatus(slaTimeVo);
                                    }
                                    continue;
                                }
                            }
                            slaTimeVo.setProcessTaskId(processTaskId);
                            if (isSlaTimeExists) {
                                processTaskMapper.updateProcessTaskSlaTime(slaTimeVo);
                            } else {
                                processTaskMapper.insertProcessTaskSlaTime(slaTimeVo);
                            }
                            adjustJob(slaTimeVo, slaConfigObj, oldExpireTime);
                        } else if (isSlaTimeExists) {
                            deleteSlaById(slaId);
                        }
                    }
                }
            }
            /** 提交事务 **/
            transactionUtil.commitTx(transactionStatus);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            /** 回滚事务 **/
            transactionUtil.rollbackTx(transactionStatus);
        }
    }

    private void deleteSlaById(Long slaId) {
        processTaskMapper.deleteProcessTaskSlaTimeBySlaId(slaId);
        processTaskMapper.deleteProcessTaskSlaTransferBySlaId(slaId);
        processTaskMapper.deleteProcessTaskSlaNotifyBySlaId(slaId);
    }
}
