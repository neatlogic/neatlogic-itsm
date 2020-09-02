package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;

public interface ProcessTaskStepSubtaskService {

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
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取步骤子任务列表
    * @param processTaskStepId 步骤id
    * @return List<ProcessTaskStepSubtaskVo>
     */
    public List<ProcessTaskStepSubtaskVo> getProcessTaskStepSubtaskListByProcessTaskStepId(Long processTaskStepId);

}
