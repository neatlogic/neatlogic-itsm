package codedriver.module.process.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.type.PermissionDeniedException;
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
	* @param processTaskVo 工单信息
	* @param formAttributeActionMap 处理页面时，表单属性只读/隐藏控制数据
	* @param mode 0：查看页面，1：处理页面
	* @return void
	 */
	public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo, Map<String, String> formAttributeActionMap, int mode);

	public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo);

	/**
	 * 执行请求
	 * @param automaticConfigVo
	 * @param currentProcessTaskStepVo
	 */
	public Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo);

//	public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId);

	public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo, AutomaticConfigVo automaticConfig,
			JSONObject data, String type);

	/**
	 * 初始化job
	 * @param automaticConfigVo
	 * @param currentProcessTaskStepVo
	 */
	public void initJob(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo, JSONObject data);
	
	/**
	 * 
	* @Author: linbq
	* @Time:2020年8月21日
	* @Description: 检查工单参数是否合法 
	* @param processTaskId 工单id
	* @param processTaskStepId 步骤id
	* @param nextStepId 下一步骤id
	* @return boolean
	 * @throws Exception 
	 */
	public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId) throws Exception;
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 检查工单参数是否合法 
    * @param processTaskId 工单id
    * @param processTaskStepId 步骤id
    * @return boolean
	 * @throws Exception 
     */
	public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception;
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 检查工单参数是否合法 
    * @param processTaskId 工单id
    * @return boolean
	 * @throws Exception 
     */
	public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception;
	/**
	 * 
	* @Author: linbq
	* @Time:2020年8月21日
	* @Description: 获取工单信息 
	* @param processTaskId 工单id
	* @return ProcessTaskVo
	 * @throws Exception 
	 */
	public ProcessTaskVo getProcessTaskDetailById(Long processTaskId) throws Exception;
	
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取步骤回复列表
    * @param processTaskStepId 步骤id
    * @return List<ProcessTaskStepCommentVo>
     */
	public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId, List<String> typeList);
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取需指派处理人的步骤列表
    * @param processTaskId 工单id
    * @param processStepUuid 流程步骤uuid
    * @return List<ProcessTaskStepVo>
     */
	public List<ProcessTaskStepVo> getAssignableWorkerStepList(Long processTaskId, String processStepUuid);
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取需指派处理人的步骤列表
    * @param processUuid 流程uuid
    * @param processStepUuid 流程步骤uuid
    * @return List<ProcessTaskStepVo>
     */
	public List<ProcessTaskStepVo> getAssignableWorkerStepList(String processUuid, String processStepUuid);
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取步骤时效列表
    * @param processTaskStepId 步骤id
    * @return List<ProcessTaskSlaTimeVo>
     */
	public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(Long processTaskStepId, String worktimeUuid);
	/**
     * 
    * @Author: linbq
    * @Time:2020年9月23日
    * @Description: 获取前进步骤列表
    * @param processTaskStepId 步骤id
    * @return List<ProcessTaskStepVo>
     */
	public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(Long processTaskStepId);
	/**
     * 
    * @Author: linbq
    * @Time:2020年9月23日
    * @Description: 获取回退步骤列表
    * @param processTaskStepId 步骤id
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
	* @Time:2020年8月24日
	* @Description: 获取开始步骤描述内容及附件列表 
	* @param processTaskStepId 步骤id
	* @return ProcessTaskStepCommentVo
	 */
	public ProcessTaskStepReplyVo getProcessTaskStepContentAndFileByProcessTaskStepId(Long processTaskStepId);
	/**
	 * 
	* @Author: linbq
	* @Time:2020年8月26日
	* @Description: TODO 
	* @param jsonObj 
	* @param processTaskStepReplyVo 旧的回复数据
	* @return boolean 如果保存成功返回true，否则返回false
	 */
	public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo processTaskStepReplyVo);

	/**
     * 
     * @Time:2020年4月2日
     * @Description: 检查当前用户是否配置该权限
     * @param processTaskVo
     * @param processTaskStepVo
     * @param operationType 
     * @return boolean
     */
	public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, ProcessTaskOperationType operationType);
	
	/**
     * 
     * @Time:2020年4月2日
     * @Description: 检查当前用户是否配置该权限
     * @param processTaskVo
     * @param processTaskStepVo
     * @param operationType 
     * @return boolean
     */
    public boolean checkOperationAuthIsConfigured(ProcessTaskVo processTaskVo, ProcessTaskOperationType operationType);
	/**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能撤回的步骤列表
     * @param processTask
     * @return Set<ProcessTaskStepVo>
     */
	public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo);
	/**
     * 
     * @Author: 14378
     * @Time:2020年4月3日
     * @Description: 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     * @param processTaskStepList 步骤列表
     * @param processTaskStepId 已激活的步骤id
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(List<ProcessTaskStepVo> processTaskStepList, Long processTaskStepId);

	/**
     * 
     * @Time:2020年4月18日
     * @Description: 获取工单中当前用户能催办的步骤列表
     * @param processTaskVo
     * @return List<ProcessTaskStepVo>
     */
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo);
    
//    public List<ProcessTaskStepVo> getProcessTaskStepVoListByProcessTask(ProcessTaskVo processTaskVo);
    
    public ProcessTaskVo getFromProcessTasById(Long processTaskId) throws Exception;

    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId);

    /**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取开始步骤信息 
    * @param processTaskId 工单id
    * @return ProcessTaskStepVo
     */
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId);

//    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskId(Long processTaskId, List<String> typeList);

    /**
    * @Author 89770
    * @Time 2020年11月5日  
    * @Description: 跟新标签
    * @Param 
    * @return
     */
    public void updateTag(Long processTaskId,Long processTaskStepId,JSONObject jsonObj)throws PermissionDeniedException;
}
