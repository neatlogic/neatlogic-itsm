package codedriver.module.process.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import codedriver.framework.notify.dto.NotifyReceiverVo;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskStepRemindVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;

public interface ProcessTaskService {
    /**
     * 
     * @Description: 工单上报/查看/处理页面，返回表单formConfig时，设置属性只读/隐藏控制数据
     * @param processTaskVo
     *            工单信息
     * @param formAttributeActionMap
     *            处理页面时，表单属性只读/隐藏控制数据
     * @param mode
     *            0：查看页面，1：处理页面
     * @return void
     */
    public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo,
        Map<String, String> formAttributeActionMap, int mode);

    public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo);

    /**
     * 执行请求
     * 
     * @param automaticConfigVo
     * @param currentProcessTaskStepVo
     */
    public Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo);

    public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo,
        AutomaticConfigVo automaticConfig, JSONObject data, String type);

    /**
     * 初始化job
     * 
     * @param automaticConfigVo
     * @param currentProcessTaskStepVo
     */
    public void initJob(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo,
        JSONObject data);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     * @param processTaskId
     *            工单id
     * @param processTaskStepId
     *            步骤id
     * @param nextStepId
     *            下一步骤id
     * @return boolean
     * @throws Exception
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId)
        throws Exception;

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     * @param processTaskId
     *            工单id
     * @param processTaskStepId
     *            步骤id
     * @return boolean
     * @throws Exception
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception;

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 检查工单参数是否合法
     * @param processTaskId
     *            工单id
     * @return boolean
     * @throws Exception
     */
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception;

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取步骤回复列表
     * @param processTaskStepId
     *            步骤id
     * @return List<ProcessTaskStepCommentVo>
     */
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId,
        List<String> typeList);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取需指派处理人的步骤列表
     * @param processTaskId
     *            工单id
     * @param processStepUuid
     *            流程步骤uuid
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(Long processTaskId, String processStepUuid);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取需指派处理人的步骤列表
     * @param processUuid
     *            流程uuid
     * @param processStepUuid
     *            流程步骤uuid
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(String processUuid, String processStepUuid);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取步骤时效列表
     * @param processTaskStepId
     *            步骤id
     * @return List<ProcessTaskSlaTimeVo>
     */
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(Long processTaskStepId,
        String worktimeUuid);

    /**
     * 
     * @Author: linbq
     * @Time:2020年9月23日
     * @Description: 获取前进步骤列表
     * @param processTaskStepId
     *            步骤id
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(Long processTaskStepId);

    /**
     * 
     * @Author: linbq
     * @Time:2020年9月23日
     * @Description: 获取回退步骤列表
     * @param processTaskStepId
     *            步骤id
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getBackwardNextStepListByProcessTaskStepId(Long processTaskStepId);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月24日
     * @Description: 设置步骤处理人、协助处理人、待办人等
     * @param processTaskStepVo
     * @return void
     */
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo);

    /**
     * 
     * @Author: linbq
     * @Time:2020年8月26日
     * @Description: TODO
     * @param jsonObj
     * @param processTaskStepReplyVo
     *            旧的回复数据
     * @return boolean 如果保存成功返回true，否则返回false
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
     * @Returns:java.util.Set<codedriver.framework.process.dto.ProcessTaskStepVo>
     **/
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid);

    /**
     * @Description: 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     * @Author: linbq
     * @Date: 2020/4/3 8:26
     * @Params:[processTaskVo, processTaskStepId, userUuid]
     * @Returns:java.util.List<codedriver.framework.process.dto.ProcessTaskStepVo>
     **/
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(ProcessTaskVo processTaskVo,
        Long processTaskStepId, String userUuid);

    /**
     * 
     * @Time:2020年4月18日
     * @Description: 获取工单中当前用户能催办的步骤列表
     * @param processTaskVo
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo, String userUuid);

    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId);

    /**
     * 
     * @Time:2020年11月26日
     * @Description: 获取当前用户有转交权限的步骤列表
     * @param processTaskVo
     * @return Set<ProcessTaskStepVo>
     */
    public Set<ProcessTaskStepVo> getTransferableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid);


    /**
     *
     * @Time:2020年8月21日
     * @Description: 获取工单信息
     * @param processTaskId
     *            工单id
     * @return ProcessTaskVo
     */
    public ProcessTaskVo getProcessTaskDetailById(Long processTaskId);

    /**
     *
     * @Time:2020年8月21日
     * @Description: 获取开始步骤信息
     * @param processTaskId
     *            工单id
     * @return ProcessTaskStepVo
     */
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId);
    /**
     *
     * @Time:2020年12月23日
     * @Description: 返回当前步骤详情
     * @param currentProcessTaskStep 当前步骤基本信息
     * @return ProcessTaskStepVo
     */
    public ProcessTaskStepVo getCurrentProcessTaskStepDetail(ProcessTaskStepVo currentProcessTaskStep);

    /**
     *
     * @Time:2020年12月3日
     * @Description: 获取来源工单信息
     * @param processTaskId
     * @return ProcessTaskVo
     */
    public ProcessTaskVo getFromProcessTasById(Long processTaskId);

    /**
     * @Description: 获取所有工单干系人信息，用于通知接收人
     * @Author: linbq
     * @Date: 2021/1/27 15:50
     * @Params:[currentProcessTaskStepVo, receiverMap]
     * @Returns:void
     **/
    public void getReceiverMap(ProcessTaskStepVo currentProcessTaskStepVo,
                               Map<String, List<NotifyReceiverVo>> receiverMap);
    /**
     * @Description: 设置步骤当前用户的暂存数据
     * @Author: linbq
     * @Date: 2021/1/27 15:47
     * @Params:[processTaskVo, processTaskStepVo]
     * @Returns:void
     **/
    public void setTemporaryData(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo);

    /**
     * @Description: 查询待处理的工单，构造"用户uuid->List<工单字段中文名->值>"的map集合
     * @Author: laiwt
     * @Date: 2021/1/8 14:23
     * @Params: [map]
     * @Returns: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     **/
    public Map<String,List<Map<String,Object>>> getProcessingUserTaskMapByCondition(Map<String,Object> map);
}
