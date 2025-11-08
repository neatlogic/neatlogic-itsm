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
package neatlogic.module.process.service;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.audithandler.core.IProcessTaskAuditType;
import neatlogic.framework.process.dto.ProcessTaskStepAssignVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stepremind.core.IProcessTaskStepRemindType;

import java.util.List;

public interface IProcessStepHandlerUtil {
    /**
     * @Description: 触发动作
     * @Author: linbq
     * @Date: 2021/1/20 16:15
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     **/
    void action(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger);

    /**
     * @Description: 触发通知
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     **/
    void notify(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger);

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     **/
    void calculateSla(ProcessTaskVo currentProcessTaskVo, boolean isAsync);

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     **/
    void calculateSla(ProcessTaskVo currentProcessTaskVo);

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     **/
    void calculateSla(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * @Description: 记录操作时间
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     **/
    void timeAudit(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType action);

    /**
     * @Description: 记录操作活动
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     **/
    void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action);

    /**
     * @Description: 自动评分
     * @Author: linbq
     * @Date: 2021/1/20 16:22
     * @Params:[currentProcessTaskVo]
     * @Returns:void
     **/
    void autoScore(ProcessTaskVo currentProcessTaskVo);

    /**
     * @Description: 获取验证基本信息数据是否合法，并验证
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     **/
    boolean baseInfoValidFromDb(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * @Description: 验证前置步骤指派处理人是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     **/
    boolean assignWorkerValid(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * 找出流转到哪些步骤时，需要指定targetStepId步骤的处理人
     * @param processTaskId 工单id
     * @param currentStepId 当前流转步骤id
     * @param targetStepId 配置了由当前步骤处理人指定处理人的步骤id
     * @return
     */
    List<Long> getNextStepIdList(Long processTaskId, Long currentStepId, Long targetStepId);

    /**
     * 找出流转到哪些步骤时，需要指定targetStepId步骤的处理人
     * @param processTaskId 工单id
     * @param currentStepId 当前流转步骤id
     * @param conditionStepUuidList 生效条件步骤列表
     * @return
     */
    List<Long> getNextStepIdList(Long processTaskId, Long currentStepId, List<String> conditionStepUuidList);

    /**
     * @Description: 保存步骤提醒
     * @Author: linbq
     * @Date: 2021/1/21 11:30
     * @Params:[currentProcessTaskStepVo, targerProcessTaskStepId, reason, ation]
     * @Returns:int
     **/
    int saveStepRemind(ProcessTaskStepVo currentProcessTaskStepVo, Long targerProcessTaskStepId, String reason, IProcessTaskStepRemindType ation);

    /**
     * @Description: 保存描述内容和附件
     * @Author: linbq
     * @Date: 2021/1/27 11:41
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     **/
    void saveContentAndFile(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType action);

    /**
     * 保存工单级别的操作描述内容
     * @param currentProcessTaskVo
     * @param action
     */
    void saveProcessTaskOperationContent(ProcessTaskVo currentProcessTaskVo, IOperationType action);

    /**
     * 流转操作时检查回复信息是否必填
     * @param currentProcessTaskStepVo
     */
    void checkContentIsRequired(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType operationType);

    /**
     * @Description: 保存标签列表
     * @Author: linbq
     * @Date: 2021/1/27 11:42
     * @Params:[currentProcessTaskStepVo]
     * @Returns:void
     **/
    void saveTagList(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * @Description: 保存工单关注人
     * @Author: laiwt
     * @Date: 2021/2/19 11:20
     * @Params: [currentProcessTaskStepVo]
     * @Returns: void
     **/
    void saveFocusUserList(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * 保存表单属性值
     * @param currentProcessTaskStepVo 当前步骤信息
     */
    void saveForm(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * 解析分配步骤处理人配置
     * @param currentProcessTaskStepVo
     * @return
     * @throws ProcessTaskException
     */
    ProcessTaskStepAssignVo analysisAssignConfig(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException;
}
