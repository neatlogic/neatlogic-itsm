package codedriver.module.process.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;

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
	public List<ProcessTaskStepSubtaskContentVo> commentSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo);
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

	/**
	 * 执行请求
	 * @param automaticConfigVo
	 * @param currentProcessTaskStepVo
	 */
	public Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo);

	public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId);

	public ProcessTaskVo getProcessTaskDetailInfoById(Long processTaskId);

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
	* @Time:2020年7月28日
	* @Description: 获取自定义按钮映射数据
	* @param processTaskStepId
	* @return Map<String,String>
	 */
	public Map<String, String> getCustomButtonTextMap(Long processTaskStepId);
}
