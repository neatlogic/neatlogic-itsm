package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;

public interface ProcessService {

	public int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException;

}
