package neatlogic.module.process.workerpolicy.handler;

import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskAssignWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class PreStepAssignWorkerPolicyHandler implements IWorkerPolicyHandler {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return WorkerPolicy.PRESTEPASSIGN.getValue();
	}

	@Override
	public String getName() {
		return WorkerPolicy.PRESTEPASSIGN.getText();
	}

	@Override
	public int isOnlyOnceExecute() {
		return 0;
	}

	//	{
//		"name": "由前置步骤处理人指定",
//		"type": "prestepassign",
//		"isChecked": 1,
//		"config": {
//			"isRequired": 1,
//			"processStepUuidList": ["ee1563d090cc4c38818f997aa4621ff7", "ee1563d090cc4c38818f997aa4621ff8"]
//		}
//	}
	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
		if(CollectionUtils.isNotEmpty(processStepUuidList)) {
			for(String processStepUuid : processStepUuidList) {
				ProcessTaskAssignWorkerVo processTaskAssignWorkerVo = new ProcessTaskAssignWorkerVo();
				processTaskAssignWorkerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				processTaskAssignWorkerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskAssignWorkerVo.setFromProcessStepUuid(processStepUuid);
				List<ProcessTaskAssignWorkerVo> processTaskAssignWorkerList = processTaskMapper.getProcessTaskAssignWorker(processTaskAssignWorkerVo);
				for(ProcessTaskAssignWorkerVo processTaskAssignWorker : processTaskAssignWorkerList) {
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskAssignWorker.getProcessTaskId(), processTaskAssignWorker.getProcessTaskStepId(), processTaskAssignWorker.getType(), processTaskAssignWorker.getUuid(), ProcessUserType.MAJOR.getValue()));
				}
			}
		}
		return processTaskStepWorkerList;
	}

}
