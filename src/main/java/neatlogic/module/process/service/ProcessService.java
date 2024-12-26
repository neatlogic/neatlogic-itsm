package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;

public interface ProcessService {

	public int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException;

	/**
	 * 删除流程相关数据
	 * @param uuid 流程uuid
	 */
	void deleteProcessRelevantData(String uuid);

	/**
	 * 保存或删除流程的依赖关系数据
	 * @param processVo 流程信息
	 * @param action 保存或删除
	 */
	void saveOrDeleteProcessDependency(ProcessVo processVo, String action);

	/**
	 * 保存或删除流程步骤的依赖关系数据
	 * @param processStepVo 流程步骤信息
	 * @param action 保存或删除
	 */
	void saveOrDeleteProcessStepDependency(ProcessStepVo processStepVo, String action);
}
