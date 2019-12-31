package codedriver.module.process.service.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.test.ProcessStepHandlerMapper;
import codedriver.module.process.dto.ProcessStepHandlerVo;

@Service
public class ProcessStepHandlerServiceImpl implements ProcessStepHandlerService {

	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;

	@Override
	public List<ProcessStepHandlerVo> getActiveProcessStepHandler() {
		return processStepHandlerMapper.getActiveProcessStepHandler();
	}

}
