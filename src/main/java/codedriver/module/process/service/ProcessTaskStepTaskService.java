/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessTaskStepTaskUserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.TaskConfigVo;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

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
     * @param id 任务id
     * @param content 回复内容
     */
    Long completeTask(Long id, String content) throws Exception;
    /**
     * 解析&校验 任务配置
     *
     * @param stepConfigHash 步骤配置hash
     * @return 任务配置
     */
    JSONObject getTaskConfig(String stepConfigHash);
    /**
     * 获取工单任务信息
     * @param processTaskStepVo 步骤vo
     */
    void getProcessTaskStepTask(ProcessTaskStepVo processTaskStepVo);

    /**
     * 获取步骤的任务策略列表及其任务列表
     * @param processTaskStepVo 步骤信息
     * @return
     */
    List<TaskConfigVo> getTaskConfigList(ProcessTaskStepVo processTaskStepVo);
}
