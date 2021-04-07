package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.ConditionParamContext;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dto.condition.ConditionConfigVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTimeAuditMapper;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
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
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.*;

/**
 * @Title: SlaHandler
 * @Package codedriver.module.process.thread
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 16:43
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class ProcessTaskSlaThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskSlaThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static WorktimeMapper worktimeMapper;
    private static ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
    private static TransactionUtil transactionUtil;
    private static ProcessTaskService processTaskService;
    @Autowired
    public void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
    }
    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }
    @Autowired
    public void setWorktimeMapper(WorktimeMapper _worktimeMapper) {
        worktimeMapper = _worktimeMapper;
    }
    @Autowired
    public void setProcessTaskStepTimeAuditMapper(ProcessTaskStepTimeAuditMapper _processTaskStepTimeAuditMapper) {
        processTaskStepTimeAuditMapper = _processTaskStepTimeAuditMapper;
    }
    @Autowired
    public void setTransactionUtil(TransactionUtil _transactionUtil) {
        transactionUtil = _transactionUtil;
    }
    private ProcessTaskStepVo currentProcessTaskStepVo;
    private ProcessTaskVo currentProcessTaskVo;

    public ProcessTaskSlaThread(){}
    public ProcessTaskSlaThread(ProcessTaskVo _currentProcessTaskVo, ProcessTaskStepVo _currentProcessTaskStepVo) {
        currentProcessTaskVo = _currentProcessTaskVo;
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        if (_currentProcessTaskStepVo != null) {
            this.setThreadName("PROCESSTASK-SLA-" + _currentProcessTaskStepVo.getId());
        } else if (_currentProcessTaskVo != null) {
            this.setThreadName("PROCESSTASK-SLA-" + _currentProcessTaskVo.getId());
        }
    }

    private static long getTimeCost(List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList,
                                    long currentTimeMillis, String worktimeUuid) {
        List<Map<String, Long>> timeList = new ArrayList<>();
        for (ProcessTaskStepTimeAuditVo auditVo : processTaskStepTimeAuditList) {
            // System.out.println(auditVo);
            Long startTime = null, endTime = null;
            if (auditVo.getActiveTimeLong() != null) {
                startTime = auditVo.getActiveTimeLong();
            } else if (auditVo.getStartTimeLong() != null) {
                startTime = auditVo.getStartTimeLong();
            }
            if (auditVo.getCompleteTimeLong() != null) {
                endTime = auditVo.getCompleteTimeLong();
            } else if (auditVo.getAbortTimeLong() != null) {
                endTime = auditVo.getAbortTimeLong();
            } else if (auditVo.getBackTimeLong() != null) {
                endTime = auditVo.getBackTimeLong();
            } else if (auditVo.getPauseTimeLong() != null) {
                endTime = auditVo.getPauseTimeLong();
            }
            if (startTime != null && endTime != null) {
                Map<String, Long> stimeMap = new HashMap<>();
                stimeMap.put("s", startTime);
                timeList.add(stimeMap);
                Map<String, Long> etimeMap = new HashMap<>();
                etimeMap.put("e", endTime);
                timeList.add(etimeMap);
            } else if (startTime != null) {
                Map<String, Long> stimeMap = new HashMap<>();
                stimeMap.put("s", startTime);
                timeList.add(stimeMap);
                Map<String, Long> etimeMap = new HashMap<>();
                etimeMap.put("e", currentTimeMillis);
                timeList.add(etimeMap);
            }
        }
        timeList.sort(new Comparator<Map<String, Long>>() {
            @Override
            public int compare(Map<String, Long> o1, Map<String, Long> o2) {
                Long t1 = null, t2 = null;
                if (o1.containsKey("s")) {
                    t1 = o1.get("s");
                } else if (o1.containsKey("e")) {
                    t1 = o1.get("e");
                }

                if (o2.containsKey("s")) {
                    t2 = o2.get("s");
                } else if (o2.containsKey("e")) {
                    t2 = o2.get("e");
                }
                return t1.compareTo(t2);
            }
        });
        Stack<Long> timeStack = new Stack<>();
        List<Map<String, Long>> newTimeList = new ArrayList<>();
        for (Map<String, Long> timeMap : timeList) {
            // System.out.println(timeMap);
            if (timeMap.containsKey("s")) {
                timeStack.push(timeMap.get("s"));
            } else if (timeMap.containsKey("e")) {
                if (!timeStack.isEmpty()) {
                    Long currentStartTimeLong = timeStack.pop();
                    if (timeStack.isEmpty()) {// 栈被清空时计算时间段
                        Map<String, Long> newTimeMap = new HashMap<>();
                        newTimeMap.put("s", currentStartTimeLong);
                        newTimeMap.put("e", timeMap.get("e"));
                        newTimeList.add(newTimeMap);
                    }
                }
            }
        }

        long sum = 0;
        for (Map<String, Long> timeMap : newTimeList) {
            sum += worktimeMapper.calculateCostTime(worktimeUuid, timeMap.get("s"), timeMap.get("e"));
        }
        return sum;
    }

    private static long getRealTimeCost(List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList,
                                        long currentTimeMillis) {
        int timeCost = 0;
        if (CollectionUtils.isNotEmpty(processTaskStepTimeAuditList)) {
            List<Map<String, Long>> timeZoneList = new ArrayList<>();
            for (ProcessTaskStepTimeAuditVo auditVo : processTaskStepTimeAuditList) {
                // System.out.println(auditVo);
                Long startTime = null, endTime = null;
                if (auditVo.getActiveTimeLong() != null) {
                    startTime = auditVo.getActiveTimeLong();
                } else if (auditVo.getStartTimeLong() != null) {
                    startTime = auditVo.getStartTimeLong();
                }

                if (auditVo.getCompleteTimeLong() != null) {
                    endTime = auditVo.getCompleteTimeLong();
                } else if (auditVo.getAbortTimeLong() != null) {
                    endTime = auditVo.getAbortTimeLong();
                } else if (auditVo.getBackTimeLong() != null) {
                    endTime = auditVo.getBackTimeLong();
                } else if (auditVo.getPauseTimeLong() != null) {
                    endTime = auditVo.getPauseTimeLong();
                }
                if (startTime != null && endTime != null) {
                    Map<String, Long> smap = new HashMap<>();
                    smap.put("s", startTime);
                    timeZoneList.add(smap);
                    Map<String, Long> emap = new HashMap<>();
                    emap.put("e", endTime);
                    timeZoneList.add(emap);
                } else if (startTime != null) {
                    Map<String, Long> smap = new HashMap<>();
                    smap.put("s", startTime);
                    timeZoneList.add(smap);
                    Map<String, Long> emap = new HashMap<>();
                    emap.put("e", currentTimeMillis);
                    timeZoneList.add(emap);
                }
            }
            timeZoneList.sort(new Comparator<Map<String, Long>>() {
                @Override
                public int compare(Map<String, Long> o1, Map<String, Long> o2) {
                    Long t1 = null, t2 = null;
                    if (o1.containsKey("s")) {
                        t1 = o1.get("s");
                    } else if (o1.containsKey("e")) {
                        t1 = o1.get("e");
                    }

                    if (o2.containsKey("s")) {
                        t2 = o2.get("s");
                    } else if (o2.containsKey("e")) {
                        t2 = o2.get("e");
                    }
                    return t1.compareTo(t2);
                }
            });

            Stack<Long> timeStack = new Stack<>();
            for (Map<String, Long> timeMap : timeZoneList) {
                // System.out.println(timeMap);
                if (timeMap.containsKey("s")) {
                    timeStack.push(timeMap.get("s"));
                } else if (timeMap.containsKey("e")) {
                    if (!timeStack.isEmpty()) {
                        Long currentStartTimeLong = timeStack.pop();
                        if (timeStack.isEmpty()) {
                            Long tmp = timeMap.get("e") - currentStartTimeLong;
                            timeCost += tmp.intValue();
                        }
                    }
                }
            }
        }
        // System.out.println("timeCost:" + timeCost);
        return timeCost;
    }

    private static long getRealtime(int time, String unit) {
        if ("hour".equals(unit)) {
            return time * 60 * 60 * 1000;
        } else if ("day".equals(unit)) {
            return time * 24 * 60 * 60 * 1000;
        } else {
            return time * 60 * 1000;
        }
    }

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
                /** 如果没有规则，则默认生效，如果有规则，以规则计算结果判断是否生效 **/
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
                        return getRealtime(time, unit);
                    } else {// 关联优先级
                        if (CollectionUtils.isNotEmpty(priorityList)) {
                            for (int p = 0; p < priorityList.size(); p++) {
                                JSONObject priorityObj = priorityList.getJSONObject(p);
                                if (priorityObj.getString("priorityUuid").equals(processTaskVo.getPriorityUuid())) {
                                    return getRealtime(priorityObj.getIntValue("time"),
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

    private void calculateExpireTime(ProcessTaskSlaTimeVo slaTimeVo, String worktimeUuid) {
        long realTimeCost = 0;
        long timeCost = 0;

        long currentTimeMillis = System.currentTimeMillis();
        List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList =
                processTaskStepTimeAuditMapper.getProcessTaskStepTimeAuditBySlaId(slaTimeVo.getSlaId());
        if (CollectionUtils.isNotEmpty(processTaskStepTimeAuditList)) {
            // 非第一次进入，进行时间扣减
            realTimeCost = getRealTimeCost(processTaskStepTimeAuditList, currentTimeMillis);
            timeCost = realTimeCost;
            if (StringUtils.isNotBlank(worktimeUuid)) {// 如果有工作时间，则计算实际消耗的工作时间
                timeCost = getTimeCost(processTaskStepTimeAuditList, currentTimeMillis, worktimeUuid);
            }
        }

        slaTimeVo.setRealTimeLeft(slaTimeVo.getTimeSum() - realTimeCost);
        slaTimeVo.setTimeLeft(slaTimeVo.getTimeSum() - timeCost);

        slaTimeVo.setRealExpireTime(new Date(currentTimeMillis + slaTimeVo.getRealTimeLeft()));
        if (StringUtils.isNotBlank(worktimeUuid)) {
            if (slaTimeVo.getTimeLeft() != null) {
                if (slaTimeVo.getTimeLeft() > 0) {
                    long expireTime =
                            WorkTimeUtil.calculateExpireTime(currentTimeMillis, slaTimeVo.getTimeLeft(), worktimeUuid);
                    slaTimeVo.setExpireTime(new Date(expireTime));
                } else {
                    long expireTime = WorkTimeUtil.calculateExpireTimeForTimedOut(currentTimeMillis,
                            -slaTimeVo.getTimeLeft(), worktimeUuid);
                    slaTimeVo.setExpireTime(new Date(expireTime));
                }

            } else {
                throw new RuntimeException("计算剩余时间失败");
            }
        } else {
            if (slaTimeVo.getTimeLeft() != null) {
                slaTimeVo.setExpireTime(new Date(currentTimeMillis + slaTimeVo.getTimeLeft()));
            } else {
                throw new RuntimeException("计算剩余时间失败");
            }
        }
    }

    private void adjustJob(ProcessTaskSlaTimeVo slaTimeVo, JSONObject slaConfigObj, Date oldExpireTime) {
        /** 有超时时间点 **/
        if (slaTimeVo.getExpireTime() != null) {
            /** 是否需要启动作业 **/
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
            /** 作业是否已启动 **/
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
            /** 作业需要启动，且未启动时，加载定时作业 **/
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
                                    new JobObject.Builder(processTaskSlaTransferVo.getId().toString(),
                                            jobHandler.getGroupName(), jobHandler.getClassName(),
                                            TenantContext.get().getTenantUuid()).addData("slaTransferId",
                                            processTaskSlaTransferVo.getId());
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
        TransactionStatus transactionStatus = transactionUtil.openTx();
        try {
            Long processTaskId = null;
            List<Long> slaIdList = null;
            if (currentProcessTaskStepVo != null) {
                slaIdList = processTaskMapper.getSlaIdListByProcessTaskStepId(currentProcessTaskStepVo.getId());
                processTaskId = currentProcessTaskStepVo.getProcessTaskId();
            } else if (currentProcessTaskVo != null) {
                slaIdList = processTaskMapper.getSlaIdListByProcessTaskId(currentProcessTaskVo.getId());
                Iterator<Long> iterator = slaIdList.iterator();
                while (iterator.hasNext()) {
                    Long slaId = iterator.next();
                    if (processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId) == null) {
                        List<ProcessTaskStepVo> processTaskStepList =
                                processTaskMapper.getProcessTaskStepBaseInfoBySlaId(slaId);
                        Iterator<ProcessTaskStepVo> it = processTaskStepList.iterator();
                        while (it.hasNext()) {
                            ProcessTaskStepVo processTaskStepVo = it.next();
                            // 未处理、处理中和挂起的步骤才需要计算SLA
                            if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
                                continue;
                            } else if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
                                continue;
                            } else if (processTaskStepVo.getStatus().equals(ProcessTaskStatus.HANG.getValue())) {
                                continue;
                            }
                            it.remove();
                        }
                        if (CollectionUtils.isEmpty(processTaskStepList)) {
                            iterator.remove();
                        }
                    }
                }
                processTaskId = currentProcessTaskVo.getId();
            }

            if (CollectionUtils.isNotEmpty(slaIdList)) {
                ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
                processTaskVo
                        .setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
                processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
                String worktimeUuid = processTaskVo.getWorktimeUuid();
                for (Long slaId : slaIdList) {
                    processTaskMapper.getProcessTaskSlaLockById(slaId);
                    String config = processTaskMapper.getProcessTaskSlaConfigById(slaId);
                    JSONObject slaConfigObj = JSON.parseObject(config);
                    if (MapUtils.isNotEmpty(slaConfigObj)) {
                        /** 旧的超时时间点 **/
                        Date oldExpireTime = null;
                        Long oldTimeSum = null;
                        boolean isSlaTimeExists = false;
                        /** 如果没有超时时间，证明第一次进入SLA标签范围，开始计算超时时间 **/
                        ProcessTaskSlaTimeVo oldSlaTimeVo = processTaskMapper.getProcessTaskSlaTimeBySlaId(slaId);
                        if (oldSlaTimeVo != null) {
                            /** 记录旧的超时时间点 **/
                            oldExpireTime = oldSlaTimeVo.getExpireTime();
                            oldTimeSum = oldSlaTimeVo.getTimeSum();
                            isSlaTimeExists = true;
                        }
                        Long timeSum = getSlaTimeSumBySlaConfig(slaConfigObj, processTaskVo);

                        // 修正最终超时日期
                        if (timeSum != null) {
                            ProcessTaskSlaTimeVo slaTimeVo = new ProcessTaskSlaTimeVo();
                            slaTimeVo.setTimeSum(timeSum);
                            slaTimeVo.setSlaId(slaId);
                            calculateExpireTime(slaTimeVo, worktimeUuid);
                            if (Objects.equals(timeSum, oldTimeSum)) {
                                long expireTimeLong = 0L;
                                long oldExpireTimeLong = 0L;
                                if(slaTimeVo.getExpireTime() != null){
                                    expireTimeLong = slaTimeVo.getExpireTime().getTime();
                                }
                                if(oldExpireTime != null){
                                    oldExpireTimeLong = oldExpireTime.getTime();
                                }
                                /** 由于Date类型数据保存到MySql数据库时会丢失毫秒数值，只保留到秒的精度，所以两次计算超时时间点的差值小于1000时，说明时效没有被条件改变，不用更新 **/
                                if(expireTimeLong - oldExpireTimeLong < 1000){
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
                            processTaskMapper.deleteProcessTaskSlaTimeBySlaId(slaId);
                            processTaskMapper.deleteProcessTaskSlaTransferBySlaId(slaId);
                            processTaskMapper.deleteProcessTaskSlaNotifyBySlaId(slaId);
                        }
                    }
                }
            }
            transactionUtil.commitTx(transactionStatus);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            transactionUtil.rollbackTx(transactionStatus);
        }
    }
}
