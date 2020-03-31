package codedriver.module.process.service;

import java.util.List;
import codedriver.framework.process.dto.ProcessTaskFormVo;
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
}
