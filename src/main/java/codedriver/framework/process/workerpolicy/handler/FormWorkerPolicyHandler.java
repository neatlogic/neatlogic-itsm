package codedriver.framework.process.workerpolicy.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.module.process.constvalue.WorkerPolicy;
import codedriver.module.process.dto.ProcessTaskAssignUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;

@Service
public class FormWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.FORM.getValue();
	}

	@Override
	public String getName() {
		return WorkerPolicy.FORM.getText();
	}
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskAssignUserVo> assignUserList = processTaskMapper.getProcessAssignUserByToStepId(currentProcessTaskStepVo.getId());
		return null;
	}
}
