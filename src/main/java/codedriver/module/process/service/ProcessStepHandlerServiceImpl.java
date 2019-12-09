package codedriver.module.process.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.mapper.ProcessStepHandlerMapper;

@Service
public class ProcessStepHandlerServiceImpl implements ProcessStepHandlerService {

	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;

	@Override
	public List<ProcessStepHandlerVo> getActiveProcessStepHandler() {
		return processStepHandlerMapper.getActiveProcessStepHandler();
	}

}
