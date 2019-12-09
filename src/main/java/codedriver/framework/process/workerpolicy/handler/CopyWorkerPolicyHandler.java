package codedriver.framework.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.mapper.ProcessTaskMapper;
import codedriver.module.process.constvalue.WorkerPolicy;

@Service
public class CopyWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.COPY.getValue();
	}

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
		if (workerPolicyVo.getConfigObj() != null && workerPolicyVo.getConfigObj().containsKey("prevId")) {
			ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
			processTaskStepVo.setProcessStepUuid(workerPolicyVo.getConfigObj().getString("prevId"));
			processTaskStepVo.setNeedPage(false);
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.searchProcessTaskStep(processTaskStepVo);
			if (processTaskStepList != null && processTaskStepList.size() > 0) {
				ProcessTaskStepVo prevStep = processTaskStepList.get(0);
				List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(prevStep.getId());
				for (ProcessTaskStepUserVo user : userList) {
					ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
					workerVo.setUserId(user.getUserId());
					workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
					workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
					workerList.add(workerVo);
				}
			}
		}
		return workerList;
	}
}
