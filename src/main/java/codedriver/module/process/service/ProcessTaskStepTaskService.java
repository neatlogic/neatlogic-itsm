/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessTaskStepTaskUserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;

/**
 * @author lvzk
 * @since 2021/8/31 11:38
 **/
public interface ProcessTaskStepTaskService {
    /**
     * 创建任务
     *
     * @param processTaskStepTaskVo 任务参数
     */
    void saveTask(ProcessTaskStepVo taskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo, boolean isCreate);

    /**
     * 完成任务
     *
     * @param processTaskStepTaskUserVo 任务用户参数
     */
    Long completeTask(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo);

    /**
     * 获取工单任务信息
     * @param processTaskStepVo 步骤vo
     */
    void getProcessTaskStepTask(ProcessTaskStepVo processTaskStepVo);

    /**
     * 刷新worker
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    void refreshWorker(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo);
}
