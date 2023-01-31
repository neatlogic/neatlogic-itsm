/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.service;

import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessTaskService {
    /**
     * @param processTaskVo 工单信息
     * @return void
     * @Description: 工单上报/查看/处理页面，返回表单formConfig时，设置属性只读/隐藏控制数据
     */
//    public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo,
//        Map<String, String> formAttributeActionMap, int mode);

    void setProcessTaskFormInfo(ProcessTaskVo processTaskVo);

    public List<String> getFormConfigAuthorityConfig(ProcessTaskVo processTaskVo);

    public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo);

    /**
     * @param processTaskId     工单id
     * @param processTaskStepId 步骤id
     * @param nextStepId        下一步骤id
     * @return boolean
     * @throws Exception
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId)
            throws Exception;

    /**
     * @param processTaskId     工单id
     * @param processTaskStepId 步骤id
     * @return boolean
     * @throws Exception
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception;

    /**
     * @param processTaskId 工单id
     * @return boolean
     * @throws Exception
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception;

    /**
     * @param processTaskStepId 步骤id
     * @return List<ProcessTaskStepCommentVo>
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取步骤回复列表
     */
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId,
                                                                                       List<String> typeList);

    /**
     * 获取需指派处理人的步骤列表
     * @param currentProcessTaskStepVo 工单步骤信息
     * @return
     */
    Map<Long, List<AssignableWorkerStepVo>> getAssignableWorkerStepMap(ProcessTaskStepVo currentProcessTaskStepVo);

    /**
     * @param processTaskStepId 步骤id
     * @return List<ProcessTaskSlaTimeVo>
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取步骤时效列表
     */
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepId(Long processTaskStepId);

    /**
     * 时效列表
     * @param slaIdList 时效ID列表
     * @return
     */
    List<ProcessTaskSlaTimeVo> getSlaTimeListBySlaIdList(List<Long> slaIdList);

    /**
     * 设置下一步骤列表
     *
     * @param processTaskStepVo 步骤信息
     */
    void setNextStepList(ProcessTaskStepVo processTaskStepVo);

    /**
     * @param processTaskStepId 步骤id
     * @return List<ProcessTaskStepVo>
     * @Author: linbq
     * @Time:2020年9月23日
     * @Description: 获取前进步骤列表
     */
    public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(Long processTaskStepId);

    /**
     * @param processTaskStepId 步骤id
     * @return List<ProcessTaskStepVo>
     * @Author: linbq
     * @Time:2020年9月23日
     * @Description: 获取回退步骤列表
     */
    public List<ProcessTaskStepVo> getBackwardNextStepListByProcessTaskStepId(Long processTaskStepId);

    /**
     * @param processTaskStepVo
     * @return void
     * @Author: linbq
     * @Time:2020年8月24日
     * @Description: 设置步骤处理人、协助处理人、待办人等
     */
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo);

    /**
     * @param jsonObj
     * @param processTaskStepReplyVo 旧的回复数据
     * @return boolean 如果保存成功返回true，否则返回false
     * @Author: linbq
     * @Time:2020年8月26日
     * @Description: TODO
     */
    public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo processTaskStepReplyVo);

    /**
     * @Description: 检查当前用户是否配置该权限
     * @Author: linbq
     * @Date: 2021/1/27 15:52
     * @Params:[processTaskStepVo, owner, reporter, operationType, userUuid]
     * @Returns:boolean
     **/
    public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, String owner, String reporter,
                                                  ProcessTaskOperationType operationType, String userUuid);

    /**
     * @Description: 检查当前用户是否配置该权限
     * @Author: linbq
     * @Date: 2020/4/2 8:25
     * @Params:[processTaskVo, operationType, userUuid]
     * @Returns:boolean
     **/
    public boolean checkOperationAuthIsConfigured(ProcessTaskVo processTaskVo, ProcessTaskOperationType operationType,
                                                  String userUuid);

    /**
     * @Description: 获取工单中当前用户能撤回的步骤列表
     * @Author: linbq
     * @Date: 2020/4/3 8:26
     * @Params:[processTaskVo, userUuid]
     * @Returns:java.util.Set<neatlogic.framework.process.dto.ProcessTaskStepVo>
     **/
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid);

    /**
     * @Description: 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     * @Author: linbq
     * @Date: 2020/4/3 8:26
     * @Params:[processTaskVo, processTaskStepId, userUuid]
     * @Returns:java.util.List<neatlogic.framework.process.dto.ProcessTaskStepVo>
     **/
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(ProcessTaskVo processTaskVo,
                                                                             Long processTaskStepId, String userUuid);

    /**
     * @param processTaskVo
     * @return List<ProcessTaskStepVo>
     * @Time:2020年4月18日
     * @Description: 获取工单中当前用户能催办的步骤列表
     */
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo, String userUuid);

    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId);

    /**
     * @param processTaskVo
     * @return Set<ProcessTaskStepVo>
     * @Time:2020年11月26日
     * @Description: 获取当前用户有转交权限的步骤列表
     */
    public Set<ProcessTaskStepVo> getTransferableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid);


    /**
     * @param processTaskVo 工单信息
     * @Time:2020年8月21日
     * @Description: 获取工单信息
     */
    void setProcessTaskDetail(ProcessTaskVo processTaskVo);

    /**
     * @param processTaskId 工单id
     * @return ProcessTaskStepVo
     * @Time:2020年8月21日
     * @Description: 获取开始步骤信息
     */
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId);

    /**
     * @param processTaskId
     * @return ProcessTaskVo
     * @Time:2020年12月3日
     * @Description: 获取来源工单信息
     */
    public ProcessTaskVo getFromProcessTaskById(Long processTaskId);

    /**
     * @Description: 获取所有工单干系人信息，用于通知接收人
     * @Author: linbq
     * @Date: 2021/1/27 15:50
     * @Params:[currentProcessTaskStepVo, receiverMap]
     * @Returns:void
     **/
    public void getReceiverMap(ProcessTaskStepVo currentProcessTaskStepVo,
                               Map<String, List<NotifyReceiverVo>> receiverMap, INotifyTriggerType notifyTriggerType);

    /**
     * @Description: 设置步骤当前用户的暂存数据
     * @Author: linbq
     * @Date: 2021/1/27 15:47
     * @Params:[processTaskVo, processTaskStepVo]
     * @Returns:void
     **/
    public void setTemporaryData(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo);

    /**
     * 查询待处理的工单，构造"用户uuid->List<工单字段中文名->值>"的map集合
     *
     * @param conditionMap 工单查询条件
     * @return "用户uuid->List<工单字段中文名->值>"的map集合
     */
    public Map<String, List<Map<String, Object>>> getProcessingUserTaskMapByCondition(Map<String, Object> conditionMap);

    /**
     * 查询每个用户待处理的工单数量，构造"用户uuid->工单数"的map集合
     *
     * @param conditionMap 工单查询条件
     * @return "用户uuid->工单数"的map集合
     */
    public Map<String, Integer> getProcessingUserTaskCountByCondition(Map<String, Object> conditionMap);

    /**
     * 获取该步骤可替换文本列表数据
     *
     * @param processTaskStepVo
     * @return
     */
    public JSONArray getReplaceableTextList(ProcessTaskStepVo processTaskStepVo);

    /**
     * 获取该步骤自定义按钮文本列表数据
     *
     * @param processTaskStepVo
     * @return
     */
    public JSONArray getCustomButtonList(ProcessTaskStepVo processTaskStepVo);

    /**
     * 获取该步骤自定义状态文本列表数据
     *
     * @param processTaskStepVo
     * @return
     */
    public JSONArray getCustomStatusList(ProcessTaskStepVo processTaskStepVo);

    /**
     * @param processTaskStepId 步骤id
     * @return
     */
    ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId);

    List<String> getProcessUserTypeList(Long processTaskId, AuthenticationInfoVo authenticationInfoVo);

    /**
     * 刷新 协助处理 worker
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    void refreshStepMinorWorker(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo);

    /**
     * 刷新 协助处理 user
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    void refreshStepMinorUser(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo);

    /**
     * @param processTaskStepVo 步骤信息
     * @return ProcessTaskStepVo
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取当前步骤信息
     */
    ProcessTaskStepVo getCurrentProcessTaskStepDetail(ProcessTaskStepVo processTaskStepVo, boolean hasComplete);

    /**
     * 根据fileId  processTaskIdList 获取对应用户是否有该工单附件的下载权限
     *
     * @param fileId            文件id
     * @param processTaskIdList 工单id
     * @return true：有权限   false：没有权限
     */
    boolean getProcessFileHasDownloadAuthWithFileIdAndProcessTaskIdList(Long fileId, List<Long> processTaskIdList);

    /**
     * 查询当前用户可以处理的步骤列表
     *
     * @param processTaskVo 工单信息
     * @param action        操作类型
     * @return
     */
    List<ProcessTaskStepVo> getProcessableStepList(ProcessTaskVo processTaskVo, String action);

    /**
     * 暂存工单草稿
     *
     * @param jsonObj
     * @return
     */
    JSONObject saveProcessTaskDraft(JSONObject jsonObj) throws Exception;

    /**
     * 提交上报工单
     *
     * @param jsonObj
     */
    void startProcessProcessTask(JSONObject jsonObj) throws Exception;

    /**
     * 完成工单步骤
     *
     * @param paramObj 参数结构见processtask/complete接口
     * @return
     * @throws Exception
     */
    void completeProcessTaskStep(JSONObject paramObj) throws Exception;

    /**
     * 开始工单步骤
     *
     * @param paramObj 参数结构见processtask/start接口
     * @return
     * @throws Exception
     */
    void startProcessTaskStep(JSONObject paramObj) throws Exception;

    /**
     * 某个用户的待办的工单中当前处理节点是打了某个标签的节点的工单列表
     *
     * @param jsonObj 参数结构见processtask/currentstepistagstepofmine/list接口
     * @return
     */
    List<Map<String, Object>> getProcessTaskListWhichIsProcessingByUserAndTag(JSONObject jsonObj);

    /**
     * 批量审批工单
     *
     * @param jsonObj 参数结构见processtask/step/batch/complete接口
     * @return
     */
    JSONObject batchCompleteProcessTaskStep(JSONObject jsonObj);

    /**
     * 检查工单状态，如果processTaskStatus属于status其中一员，则返回对应的异常对象，否则返回null
     *
     * @param processTaskStatus 工单状态
     * @param statuss           状态列表
     * @return
     */
    ProcessTaskPermissionDeniedException checkProcessTaskStatus(String processTaskStatus, ProcessTaskStatus... statuss);

    /**
     * 检查步骤状态，如果stepStatus属于status其中一员，则返回对应的异常对象，否则返回null
     *
     * @param stepStatus 步骤状态
     * @param statuss    状态列表
     * @return
     */
    ProcessTaskPermissionDeniedException checkProcessTaskStepStatus(String stepStatus, ProcessTaskStatus... statuss);

    /**
     * 获取步骤回复模版
     *
     * @param processStepUuid      步骤uuid
     * @param authenticationInfoVo 用户授权
     * @return
     */
    ProcessCommentTemplateVo getProcessStepCommentTemplate(String processStepUuid, AuthenticationInfoVo authenticationInfoVo);
//    /**
//     * 判断当前用户是否拥有工单转报权限
//     * @param processTaskVo 工单信息
//     * @param userUuid 用户uuid
//     * @return
//     */
//    boolean checkTransferReportAuthorization(ProcessTaskVo processTaskVo, String userUuid);
//
//    /**
//     * 判断当前用户是否拥有工单转报权限
//     * @param processTaskVo 工单信息
//     * @param userUuid 用户uuid
//     * @param relationId 转报关系id
//     * @return
//     */
//    boolean checkTransferReportAuthorization(ProcessTaskVo processTaskVo, String userUuid, Long relationId);
}
