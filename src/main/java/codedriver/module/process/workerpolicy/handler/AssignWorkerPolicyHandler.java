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
import codedriver.framework.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.framework.process.constvalue.UserType;
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
				if(UserType.OWNER.getValue().equals(split[1])) {
					ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, processTaskVo.getOwner(), ProcessTaskStepWorkerAction.HANDLE.getValue()));
				}else if(UserType.REPORTER.getValue().equals(split[1])) {
					ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, processTaskVo.getReporter(), ProcessTaskStepWorkerAction.HANDLE.getValue()));
				}else if(UserType.AGENT.getValue().equals(split[1])) {
					//TODO linbq代办人获取逻辑以后再实现
				}
			}else if(GroupSearch.USER.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, null, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, null, null, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}
		}
		return processTaskStepWorkerList;
	}

}
