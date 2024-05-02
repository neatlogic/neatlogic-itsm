package neatlogic.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;

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

	@Override
	public int isOnlyOnceExecute() {
		return 0;
	}

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (MapUtils.isNotEmpty(workerPolicyVo.getConfigObj())) {
			String processStepUuid = workerPolicyVo.getConfigObj().getString("processStepUuid");
			if(StringUtils.isNotBlank(processStepUuid)) {
				ProcessTaskStepVo processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuid(currentProcessTaskStepVo.getProcessTaskId(), processStepUuid);
				if(processTaskStep != null) {
				    List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStep.getId(),ProcessUserType.MAJOR.getValue());
                    for (ProcessTaskStepUserVo user : userList) {
                        processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), user.getUserUuid(), ProcessUserType.MAJOR.getValue()));
                    }
				}
			}
		}
		return processTaskStepWorkerList;
	}
}
