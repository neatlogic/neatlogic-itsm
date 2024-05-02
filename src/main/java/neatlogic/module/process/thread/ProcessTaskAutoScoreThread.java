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

package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.score.ProcessTaskAutoScoreVo;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
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
                    jobHandler.reloadJob(jobObject);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskAutoScoreJob.class.getName());
                }
            }
        }
    }
}
