package codedriver.framework.process.workerpolicy.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessTaskAssignUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.mapper.ProcessTaskMapper;
import codedriver.module.process.constvalue.WorkerPolicy;

@Service
public class FormWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.FORM.getValue();
	}

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskAssignUserVo> assignUserList = processTaskMapper.getProcessAssignUserByToStepId(currentProcessTaskStepVo.getId());
		return null;
	}
}
