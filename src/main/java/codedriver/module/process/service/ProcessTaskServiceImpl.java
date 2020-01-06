package codedriver.module.process.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo){
		return processTaskMapper.getProcessTaskStepFormAttributeByStepId(processTaskStepFormAttributeVo);
	}

	@Override
	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId) {
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		ProcessTaskFormVo form = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskStepVo.getProcessTaskId());
		if (form != null) {
			processTaskStepVo.setFormUuid(form.getFormUuid());
		}
		return processTaskStepVo;
	}

	@Override
	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId) {
		return processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
	}

	@Override
	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId) {
		return processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
	}
}
