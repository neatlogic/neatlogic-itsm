/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.schedule.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import neatlogic.framework.process.dao.mapper.ProcessTaskSlaMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTransferVo;
import neatlogic.framework.process.dto.ProcessTaskSlaVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;

import javax.annotation.Resource;

@Component
@DisallowConcurrentExecution
public class ProcessTaskSlaTransferJob extends JobBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskSlaTransferJob.class);

    private final static Integer INTERVAL_IN_SECONDS = 60 * 60;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Boolean isMyHealthy(JobObject jobObject) {
        Long slaTransferId = Long.valueOf(jobObject.getJobName());
        ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskSlaMapper.getProcessTaskSlaTransferById(slaTransferId);
        if (processTaskSlaTransferVo == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        String tenantUuid = jobObject.getTenantUuid();
        TenantContext.get().switchTenant(tenantUuid);
        Long slaTransferId = Long.valueOf(jobObject.getJobName());
        ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskSlaMapper.getProcessTaskSlaTransferById(slaTransferId);
        if (processTaskSlaTransferVo == null) {
            return;
        }
        ProcessTaskSlaTimeVo slaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(processTaskSlaTransferVo.getSlaId());
        if (slaTimeVo == null) {
            return;
        }
        boolean isJobLoaded = false;
        JSONObject policyObj = processTaskSlaTransferVo.getConfigObj();
        if (MapUtils.isNotEmpty(policyObj)) {
            String transferTo = policyObj.getString("transferTo");
            if (StringUtils.isNotBlank(transferTo)) {
                String expression = policyObj.getString("expression");
                int time = policyObj.getIntValue("time");
                String unit = policyObj.getString("unit");
                Calendar transferDate = Calendar.getInstance();
                transferDate.setTime(slaTimeVo.getExpireTime());
                if (expression.equalsIgnoreCase("before")) {
                    time = -time;
                }
                if (StringUtils.isNotBlank(unit) && time != 0) {
                    if (unit.equalsIgnoreCase("day")) {
                        transferDate.add(Calendar.DAY_OF_MONTH, time);
                    } else if (unit.equalsIgnoreCase("hour")) {
                        transferDate.add(Calendar.HOUR, time);
                    } else {
                        transferDate.add(Calendar.MINUTE, time);
                    }
                }
                /** 如果触发时间在当前时间之前，则将触发时间改为当前时间 **/
                if (transferDate.before(Calendar.getInstance())) {
                    transferDate = Calendar.getInstance();
                }
                JobObject.Builder newJobObjectBuilder = new JobObject.Builder(
                        slaTransferId.toString(),
                        this.getGroupName(),
                        this.getClassName(),
                        TenantContext.get().getTenantUuid()
                ).withBeginTime(transferDate.getTime())
                        .withIntervalInSeconds(INTERVAL_IN_SECONDS);
                JobObject newJobObject = newJobObjectBuilder.build();
                Date triggerDate = schedulerManager.loadJob(newJobObject);
                if (triggerDate != null) {
                    // 更新通知记录时间
                    processTaskSlaTransferVo.setTriggerTime(triggerDate);
                    processTaskSlaMapper.updateProcessTaskSlaTransfer(processTaskSlaTransferVo);
                    isJobLoaded = true;
                }
            }
        }
        if (!isJobLoaded) {
            // 没有加载到作业，则删除通知记录
            processTaskSlaMapper.deleteProcessTaskSlaTransferById(slaTransferId);
        }
    }

    @Override
    public void initJob(String tenantUuid) {
        List<ProcessTaskSlaTransferVo> slaTransferList = processTaskSlaMapper.getAllProcessTaskSlaTransfer();
        for (ProcessTaskSlaTransferVo processTaskSlaTransferVo : slaTransferList) {
            JobObject.Builder jobObjectBuilder = new JobObject.Builder(
                    processTaskSlaTransferVo.getId().toString(),
                    this.getGroupName(), this.getClassName(),
                    TenantContext.get().getTenantUuid()
            );
            JobObject jobObject = jobObjectBuilder.build();
            this.reloadJob(jobObject);
        }
    }

    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
        Long slaTransferId = Long.valueOf(jobObject.getJobName());
        ProcessTaskSlaTransferVo processTaskSlaTransferVo = processTaskSlaMapper.getProcessTaskSlaTransferById(slaTransferId);
        try {
            if (processTaskSlaTransferVo != null) {
                Long slaId = processTaskSlaTransferVo.getSlaId();
                ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(slaId);
                ProcessTaskSlaVo processTaskSlaVo = processTaskSlaMapper.getProcessTaskSlaById(slaId);
                JSONObject policyObj = processTaskSlaTransferVo.getConfigObj();
                if (processTaskSlaVo != null && processTaskSlaTimeVo != null && MapUtils.isNotEmpty(policyObj)) {
                    String transferTo = policyObj.getString("transferTo");
                    if (StringUtils.isNotBlank(transferTo) && transferTo.contains("#")) {
                        boolean transferToIsExists = true;//转交对象是否存在、合法
                        String[] split = transferTo.split("#");
                        if (GroupSearch.USER.getValue().equals(split[0])) {
                            if (userMapper.checkUserIsExists(split[1]) == 0) {
                                throw new UserNotFoundException(split[1]);
                            }
                        } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                            if (teamMapper.checkTeamIsExists(split[1]) == 0) {
                                throw new TeamNotFoundException(split[1]);
                            }
                        } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                            if (roleMapper.checkRoleIsExists(split[1]) == 0) {
                                throw new RoleNotFoundException(split[1]);
                            }
                        } else {
                            transferToIsExists = false;
                        }
                        if (transferToIsExists) {
                            ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
                            workerVo.setProcessTaskId(processTaskSlaVo.getProcessTaskId());
                            workerVo.setType(split[0]);
                            workerVo.setUuid(split[1]);
                            workerVo.setUserType(ProcessUserType.MAJOR.getValue());
                            /** 执行转交前，设置当前用户为system,用于权限校验 **/
                            UserContext.init(SystemUser.SYSTEM.getUserVo(), SystemUser.SYSTEM.getTimezone());
                            List<Long> processTaskStepIdList = processTaskSlaMapper.getProcessTaskStepIdListBySlaId(slaId);
                            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                                    // 激活步骤才需要转交
                                    if (processTaskStepVo.getIsActive().equals(1)) {
                                        List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
                                        workerVo.setProcessTaskStepId(processTaskStepVo.getId());
                                        workerList.add(workerVo);
                                        IProcessStepHandler stepHandler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
                                        if (stepHandler != null) {
                                            stepHandler.transfer(processTaskStepVo, workerList);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            schedulerManager.unloadJob(jobObject);
            if (processTaskSlaTransferVo != null) {
                // 删除转交记录
                processTaskSlaMapper.deleteProcessTaskSlaTransferById(processTaskSlaTransferVo.getId());
            }
        }
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PROCESSTASK-SLA-TRANSFER";
    }

}
