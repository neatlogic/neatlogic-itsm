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

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.score.ProcessTaskAutoScoreVo;
import neatlogic.module.process.schedule.plugin.ProcessTaskAutoScoreJob;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import com.alibaba.fastjson.JSONPath;
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
                    jobHandler.reloadJob(jobObject);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskAutoScoreJob.class.getName());
                }
            }
        }
    }
}
