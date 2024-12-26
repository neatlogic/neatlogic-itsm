package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;

public interface ProcessService {

	int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException;

	/**
	 * 保存或删除流程的依赖关系数据
	 * @param processVo 流程信息
	 * @param action 保存或删除
	 */
	void saveOrDeleteProcessDependency(ProcessVo processVo, String action);
}
