package codedriver.module.process.service;

import java.util.List;
import java.util.Map;

import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;

public interface ProcessTaskService {
	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId);

	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId);

	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId);

	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId);

	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo);

	/**
	 * 
	* @Description: 创建子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void createSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 编辑子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void editSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 打回重做子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void redoSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 完成子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void completeSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 取消子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void abortSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 回复子任务 
	* @param processTaskStepSubtaskVo 
	* @return void
	 */
	public void commentSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
	/**
	 * 
	* @Description: 工单上报/查看/处理页面，返回表单formConfig时，设置属性只读/隐藏控制数据
	* @param processTaskVo 工单信息
	* @param formAttributeActionMap 处理页面时，表单属性只读/隐藏控制数据
	* @param mode 0：查看页面，1：处理页面
	* @return void
	 */
	public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo, Map<String, String> formAttributeActionMap, int mode);

	public void parseProcessTaskStepComment(ProcessTaskStepCommentVo processTaskStepCommentVo);
}
