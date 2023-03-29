package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;

public interface ProcessService {

	public int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException;

	/**
	 * 删除流程相关数据
	 * @param uuid 流程uuid
	 */
	void deleteProcessRelevantData(String uuid);
}
