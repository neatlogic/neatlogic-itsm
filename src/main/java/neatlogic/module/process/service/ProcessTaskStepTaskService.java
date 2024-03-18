/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.service;

import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
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
     * @param button 按钮
     * @param source 来源
     */
    Long completeTask(Long id, String content, String button, String source) throws Exception;
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

    /**
     * @param processTaskStepTaskUserVo
     * @return void
     * @Time:2020年9月30日
     * @Description: 步骤主处理人校正操作 判断当前用户是否是代办人，如果不是就什么都不做，如果是，进行下面3个操作 1.往processtask_step_agent表中插入一条数据，记录该步骤的原主处理人和代办人
     * 2.将processtask_step_worker表中该步骤的主处理人uuid改为代办人(当前用户)
     * 3.将processtask_step_user表中该步骤的主处理人user_uuid改为代办人(当前用户)
     */
    void stepMinorUserRegulate(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo);

    /**
     * 判断当前用户是否可以处理任务
     * @param processTaskVo 工单信息
     * @param processTaskStepVo 步骤信息
     * @param stepTaskUserUuid 任务处理人uuid
     * @param stepTaskUserUuid 任务处理人id
     * @return
     */
    int checkIsReplyable(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo, String stepTaskUserUuid, Long stepTaskUserId) throws ProcessTaskPermissionDeniedException;
}
