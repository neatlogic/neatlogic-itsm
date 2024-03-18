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
public class ProcessTaskSlaServiceImpl implements ProcessTaskSlaService {

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
