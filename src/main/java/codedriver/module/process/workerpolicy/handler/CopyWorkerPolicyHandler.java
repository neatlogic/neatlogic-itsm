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
		if (MapUtils.isNotEmpty(workerPolicyVo.getConfigObj())) {
			String processStepUuid = workerPolicyVo.getConfigObj().getString("processStepUuidList");
			if(StringUtils.isNotBlank(processStepUuid)) {
				ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
				processTaskStepVo.setProcessStepUuid(processStepUuid);
				processTaskStepVo.setNeedPage(false);
				List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.searchProcessTaskStep(processTaskStepVo);
				if(CollectionUtils.isNotEmpty(processTaskStepList)) {
					for(ProcessTaskStepVo processTaskStep : processTaskStepList) {
						if(processTaskStep.getProcessTaskId().equals(currentProcessTaskStepVo.getProcessTaskId())) {
							List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStep.getId(),UserType.MAJOR.getValue());
							for (ProcessTaskStepUserVo user : userList) {
								processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), user.getUserId(), ProcessTaskStepWorkerAction.HANDLE.getValue()));
							}
						}
					}
				}
			}
		}
		return processTaskStepWorkerList;
	}
}
