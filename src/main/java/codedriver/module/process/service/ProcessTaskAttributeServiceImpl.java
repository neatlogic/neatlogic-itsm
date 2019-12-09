package codedriver.module.process.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.ProcessTaskAttributeMapper;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;

@Service
public class ProcessTaskAttributeServiceImpl implements ProcessTaskAttributeService {

	@Autowired
	private ProcessTaskAttributeMapper processTaskAttributeMapper;

	@Override
	public List<ProcessTaskStepAttributeVo> getProcessTaskStepAttributeListByProcessTaskStepId(Long processTaskId, Long processTaskStepId) {
		return processTaskAttributeMapper.getProcessAttributeListByProcessTaskAndStepId(processTaskId, processTaskStepId);
	}

}
