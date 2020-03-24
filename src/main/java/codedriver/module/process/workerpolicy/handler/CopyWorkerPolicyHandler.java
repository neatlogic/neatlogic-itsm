package codedriver.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.framework.process.constvalue.UserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;

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
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (MapUtils.isEmpty(workerPolicyVo.getConfigObj())) {
			return processTaskStepWorkerList;
		}
		String processStepUuidList = workerPolicyVo.getConfigObj().getString("processStepUuidList");
		if(StringUtils.isBlank(processStepUuidList)) {
			return processTaskStepWorkerList;
		}
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessStepUuid(processStepUuidList);
		processTaskStepVo.setNeedPage(false);
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.searchProcessTaskStep(processTaskStepVo);
		if(CollectionUtils.isEmpty(processTaskStepList)) {
			return processTaskStepWorkerList;
		}
		ProcessTaskStepVo prevStep = processTaskStepList.get(0);
		List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(prevStep.getId(),UserType.MAJOR.getValue());
		for (ProcessTaskStepUserVo user : userList) {
			processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), user.getUserId(), ProcessTaskStepWorkerAction.HANDLE.getValue()));
		}
		
		return processTaskStepWorkerList;
	}
}