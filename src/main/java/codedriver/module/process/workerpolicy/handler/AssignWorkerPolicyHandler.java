package codedriver.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;

@Service
public class AssignWorkerPolicyHandler implements IWorkerPolicyHandler {
	Logger logger = LoggerFactory.getLogger(AssignWorkerPolicyHandler.class);
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return WorkerPolicy.ASSIGN.getValue();
	}

	@Override
	public String getName() {
		return WorkerPolicy.ASSIGN.getText();
	}

	@Override
	public int isOnlyOnceExecute() {
		return 0;
	}

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (MapUtils.isEmpty(workerPolicyVo.getConfigObj())) {
			return processTaskStepWorkerList;
		}
		JSONArray workerList = workerPolicyVo.getConfigObj().getJSONArray("workerList");
		if(CollectionUtils.isEmpty(workerList)) {
			return processTaskStepWorkerList;
		}
		Long processTaskId = currentProcessTaskStepVo.getProcessTaskId();
		Long processTaskStepId = currentProcessTaskStepVo.getId();
		for(int j = 0; j < workerList.size(); j++) {
			String worker = workerList.getString(j);
			String[] split = worker.split("#");
			if(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
				if(ProcessUserType.OWNER.getValue().equals(split[1])) {
					ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, GroupSearch.USER.getValue(), processTaskVo.getOwner(), ProcessUserType.MAJOR.getValue()));
				}else if(ProcessUserType.REPORTER.getValue().equals(split[1])) {
					ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, GroupSearch.USER.getValue(), processTaskVo.getReporter(), ProcessUserType.MAJOR.getValue()));
				}
			}else if(GroupSearch.getValue(split[0]) != null) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, split[0], split[1], ProcessUserType.MAJOR.getValue()));
			}
		}
		return processTaskStepWorkerList;
	}

}
