package codedriver.framework.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.constvalue.WorkerPolicy;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;

@Service
public class CopyWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.COPY.getValue();
	}
	
	@Override
	public String getName() {
		return WorkerPolicy.COPY.getText();
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
				List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(prevStep.getId(),UserType.MAJOR.getValue());
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
