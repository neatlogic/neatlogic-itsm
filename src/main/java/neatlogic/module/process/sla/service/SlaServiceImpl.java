/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.sla.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dao.mapper.ProcessTaskSlaMapper;
import neatlogic.framework.process.dto.ProcessTaskSlaNotifyVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeCostVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTransferVo;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.WorkTimeUtil;
import neatlogic.module.process.schedule.plugin.ProcessTaskSlaNotifyJob;
import neatlogic.module.process.schedule.plugin.ProcessTaskSlaTransferJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SlaServiceImpl implements SlaService {

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Override
    public ProcessTaskSlaTimeVo createSlaTime(Long slaId, Long timeSum, long currentTimeMillis, ProcessTaskSlaTimeCostVo timeCostVo) {
        ProcessTaskSlaTimeVo slaTimeVo = new ProcessTaskSlaTimeVo();
        slaTimeVo.setCalculationTime(new Date(currentTimeMillis));
        slaTimeVo.setTimeSum(timeSum);
        slaTimeVo.setSlaId(slaId);
        long realTimeLeft = timeSum - timeCostVo.getRealTimeCost();
        long timeLeft = timeSum - timeCostVo.getTimeCost();
        slaTimeVo.setRealTimeLeft(realTimeLeft);
        slaTimeVo.setTimeLeft(timeLeft);

        return slaTimeVo;
    }

    @Override
    public void recalculateExpireTime(ProcessTaskSlaTimeVo slaTimeVo, long currentTimeMillis, String worktimeUuid) {
        long realExpireTimeLong = currentTimeMillis + slaTimeVo.getRealTimeLeft();
        slaTimeVo.setRealExpireTimeLong(realExpireTimeLong);
        slaTimeVo.setRealExpireTime(new Date(realExpireTimeLong));
        long timeLeft = slaTimeVo.getTimeLeft();
        if (StringUtils.isNotBlank(worktimeUuid)) {
            if (timeLeft > 0) {
                long expireTime = WorkTimeUtil.calculateExpireTime(currentTimeMillis, timeLeft, worktimeUuid);
                slaTimeVo.setExpireTimeLong(expireTime);
                slaTimeVo.setExpireTime(new Date(expireTime));
            } else {
                long expireTime = WorkTimeUtil.calculateExpireTimeForTimedOut(currentTimeMillis, -timeLeft, worktimeUuid);
                slaTimeVo.setExpireTimeLong(expireTime);
                slaTimeVo.setExpireTime(new Date(expireTime));
            }
        } else {
            long expireTime = currentTimeMillis + timeLeft;
            slaTimeVo.setExpireTimeLong(expireTime);
            slaTimeVo.setExpireTime(new Date(expireTime));
        }
    }

    @Override
    public void loadJobNotifyAndTransfer(Long slaId, JSONObject slaConfigObj) {
        // 加载定时作业，执行超时通知操作
        JSONArray notifyPolicyList = slaConfigObj.getJSONArray("notifyPolicyList");
        if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
            List<ProcessTaskSlaNotifyVo> processTaskSlaNotifyList = processTaskSlaMapper.getProcessTaskSlaNotifyBySlaId(slaId);
            if (CollectionUtils.isNotEmpty(processTaskSlaNotifyList)) {
//                    System.out.println("删除时效id=" + slaId + "的job，因为超时时间点变了");
                processTaskSlaMapper.deleteProcessTaskSlaNotifyBySlaId(slaId);
            }
            for (int i = 0; i < notifyPolicyList.size(); i++) {
                JSONObject notifyPolicyObj = notifyPolicyList.getJSONObject(i);
                ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = new ProcessTaskSlaNotifyVo();
                processTaskSlaNotifyVo.setSlaId(slaId);
                processTaskSlaNotifyVo.setConfig(notifyPolicyObj.toJSONString());
                // 需要发通知时写入数据，执行完毕后清除
                processTaskSlaMapper.insertProcessTaskSlaNotify(processTaskSlaNotifyVo);
                IJob jobHandler = SchedulerManager.getHandler(ProcessTaskSlaNotifyJob.class.getName());
                if (jobHandler != null) {
                    JobObject.Builder jobObjectBuilder =
                            new JobObject.Builder(
                                    processTaskSlaNotifyVo.getId().toString(),
                                    jobHandler.getGroupName(),
                                    jobHandler.getClassName(),
                                    TenantContext.get().getTenantUuid()
                            );
                    JobObject jobObject = jobObjectBuilder.build();
//                    System.out.println("loadJobNotifyAndtransfer....");
                    jobHandler.reloadJob(jobObject);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskSlaNotifyJob.class.getName());
                }
            }
        }
        // 加载定时作业，执行超时转交操作
        JSONArray transferPolicyList = slaConfigObj.getJSONArray("transferPolicyList");
        if (CollectionUtils.isNotEmpty(transferPolicyList)) {
            List<ProcessTaskSlaTransferVo> processTaskSlaTransferList = processTaskSlaMapper.getProcessTaskSlaTransferBySlaId(slaId);
            if (CollectionUtils.isNotEmpty(processTaskSlaTransferList)) {
                processTaskSlaMapper.deleteProcessTaskSlaTransferBySlaId(slaId);
            }
            for (int i = 0; i < transferPolicyList.size(); i++) {
                JSONObject transferPolicyObj = transferPolicyList.getJSONObject(i);
                ProcessTaskSlaTransferVo processTaskSlaTransferVo = new ProcessTaskSlaTransferVo();
                processTaskSlaTransferVo.setSlaId(slaId);
                processTaskSlaTransferVo.setConfig(transferPolicyObj.toJSONString());
                // 需要转交时写入数据，执行完毕后清除
                processTaskSlaMapper.insertProcessTaskSlaTransfer(processTaskSlaTransferVo);
                IJob jobHandler = SchedulerManager.getHandler(ProcessTaskSlaTransferJob.class.getName());
                if (jobHandler != null) {
                    JobObject.Builder jobObjectBuilder =
                            new JobObject.Builder(
                                    processTaskSlaTransferVo.getId().toString(),
                                    jobHandler.getGroupName(),
                                    jobHandler.getClassName(),
                                    TenantContext.get().getTenantUuid()
                            );
                    JobObject jobObject = jobObjectBuilder.build();
                    jobHandler.reloadJob(jobObject);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskSlaTransferVo.class.getName());
                }
            }
        }
    }
}
