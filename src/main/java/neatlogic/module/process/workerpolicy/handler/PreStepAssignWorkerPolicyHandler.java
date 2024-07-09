package neatlogic.module.process.workerpolicy.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.framework.process.dto.ProcessTaskAssignWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
		JSONObject configObj = workerPolicyVo.getConfigObj();
		if (MapUtils.isNotEmpty(configObj)) {
			JSONArray processStepList = configObj.getJSONArray("processStepList");
			if (CollectionUtils.isNotEmpty(processStepList)) {
				for (int i = 0; i < processStepList.size(); i++) {
					JSONObject processStepObj = processStepList.getJSONObject(i);
					if (MapUtils.isNotEmpty(processStepObj)) {
						String uuid = processStepObj.getString("uuid");
						if (StringUtils.isNotEmpty(uuid)) {
							ProcessTaskAssignWorkerVo processTaskAssignWorkerVo = new ProcessTaskAssignWorkerVo();
							processTaskAssignWorkerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							processTaskAssignWorkerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
							processTaskAssignWorkerVo.setFromProcessStepUuid(uuid);
							List<ProcessTaskAssignWorkerVo> processTaskAssignWorkerList = processTaskMapper.getProcessTaskAssignWorker(processTaskAssignWorkerVo);
							for (ProcessTaskAssignWorkerVo processTaskAssignWorker : processTaskAssignWorkerList) {
								processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskAssignWorker.getProcessTaskId(), processTaskAssignWorker.getProcessTaskStepId(), processTaskAssignWorker.getType(), processTaskAssignWorker.getUuid(), ProcessUserType.MAJOR.getValue()));
							}
						}
					}
				}
			} else {
				JSONArray processStepUuidArray = configObj.getJSONArray("processStepUuidList");
				if(CollectionUtils.isNotEmpty(processStepUuidArray)) {
					List<String> processStepUuidList = processStepUuidArray.toJavaList(String.class);
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
			}
		}
		return processTaskStepWorkerList;
	}

}
