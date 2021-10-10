/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.score.ProcessTaskAutoScoreVo;
import codedriver.module.process.schedule.plugin.ProcessTaskAutoScoreJob;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import com.alibaba.fastjson.JSONPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProcessTaskAutoScoreThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
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
                                    jobHandler.getClassName(), TenantContext.get().getTenantUuid()).addData("processTaskId",
                                    currentProcessTaskVo.getId());
                    JobObject jobObject = jobObjectBuilder.build();
                    jobHandler.reloadJob(jobObject);
                } else {
                    throw new ScheduleHandlerNotFoundException(ProcessTaskAutoScoreJob.class.getName());
                }
            }
        }
    }
}
