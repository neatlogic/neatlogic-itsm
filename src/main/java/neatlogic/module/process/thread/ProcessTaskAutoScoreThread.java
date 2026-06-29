/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.score.ProcessTaskAutoScoreVo;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.enums.JobLoadTriggerType;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.module.process.schedule.plugin.ProcessTaskAutoScoreJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProcessTaskAutoScoreThread extends NeatLogicThread {
    private static ProcessTaskMapper processTaskMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static ProcessTaskScoreMapper processTaskScoreMapper;

    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Autowired
    public void setSelectContentByHashMapper(SelectContentByHashMapper _selectContentByHashMapper) {
        selectContentByHashMapper = _selectContentByHashMapper;
    }

    @Autowired
    public void setProcessTaskScoreMapper(ProcessTaskScoreMapper _processTaskScoreMapper) {
        processTaskScoreMapper = _processTaskScoreMapper;
    }

    private ProcessTaskVo currentProcessTaskVo;

    public ProcessTaskAutoScoreThread() {
        super("PROCESSTASK-AUTOSCORE");
    }

    public ProcessTaskAutoScoreThread(ProcessTaskVo _currentProcessTaskVo) {
        super("PROCESSTASK-AUTOSCORE-" + _currentProcessTaskVo.getId());
        currentProcessTaskVo = _currentProcessTaskVo;
    }

    @Override
    public void execute() {

        /*
          先检查是否设置自动评分 如果设置了自动评分，则启动定时器监听工单是否评分，若超时未评分，则系统自动评分
         */
        ProcessTaskVo task = processTaskMapper.getProcessTaskById(currentProcessTaskVo.getId());
        if (task != null) {
            String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(task.getConfigHash());
            Integer isAuto = (Integer) JSONPath.read(config, "process.scoreConfig.isAuto");
            if (Objects.equals(isAuto, 1)) {
                IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutoScoreJob.class.getName());
                if (jobHandler != null) {
                    ProcessTaskAutoScoreVo processTaskAutoScoreVo = new ProcessTaskAutoScoreVo();
                    processTaskAutoScoreVo.setProcessTaskId(task.getId());
                    processTaskAutoScoreVo.setConfig(JSONPath.read(config, "process.scoreConfig").toString());
                    processTaskScoreMapper.insertProcessTaskAutoScore(processTaskAutoScoreVo);
                    JobObject.Builder jobObjectBuilder =
                            new JobObject.Builder(currentProcessTaskVo.getId().toString(), jobHandler.getGroupName(),
                                    jobHandler.getClassName(), TenantContext.get().getTenantUuid());
                    JobObject jobObject = jobObjectBuilder.build();
                    jobHandler.reloadJob(jobObject, JobLoadTriggerType.INITIAL_CREATE);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskAutoScoreJob.class.getName());
                }
            }
        }
    }
}
