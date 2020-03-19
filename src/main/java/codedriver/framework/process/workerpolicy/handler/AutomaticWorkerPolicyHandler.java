package codedriver.framework.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workerdispatcher.core.IWorkerDispatcher;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.module.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.module.process.constvalue.WorkerPolicy;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;

@Service
public class AutomaticWorkerPolicyHandler implements IWorkerPolicyHandler {
	Logger logger = LoggerFactory.getLogger(AutomaticWorkerPolicyHandler.class);

	@Override
	public String getType() {
		return WorkerPolicy.AUTOMATIC.getValue();
	}

	@Override
	public String getName() {
		return WorkerPolicy.AUTOMATIC.getText();
	}
	
	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (CollectionUtils.isEmpty(workerPolicyVo.getConfigObj())) {
			return processTaskStepWorkerList;
		}
		String handler = workerPolicyVo.getConfigObj().getString("handler");
		if(StringUtils.isBlank(handler)) {
			return processTaskStepWorkerList;
		}
		IWorkerDispatcher dispatcher = WorkerDispatcherFactory.getDispatcher(handler);
		if(dispatcher == null) {
			return processTaskStepWorkerList;
		}
		JSONObject handlerConfig = workerPolicyVo.getConfigObj().getJSONObject("handlerConfig");
		if(CollectionUtils.isEmpty(handlerConfig)) {
			return processTaskStepWorkerList;
		}
		List<String> workerList = dispatcher.getWorker(currentProcessTaskStepVo, handlerConfig);
		if(CollectionUtils.isEmpty(workerList)) {
			return processTaskStepWorkerList;
		}
		for(String userId : workerList) {
			if (StringUtils.isBlank(userId)) {
				continue;
			}
			ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), userId, ProcessTaskStepWorkerAction.HANDLE.getValue());
			if (!processTaskStepWorkerList.contains(workerVo)) {
				processTaskStepWorkerList.add(workerVo);
			}
		}
		return processTaskStepWorkerList;
//		List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
//		if (workerPolicyVo.getConfigObj() != null) {
//			String handler = workerPolicyVo.getConfigObj().getString("handler");
//			JSONObject config = workerPolicyVo.getConfigObj().getJSONObject("config");
//			if (StringUtils.isNotBlank(handler)) {
//				IWorkerDispatcher dispatcher = WorkerDispatcherFactory.getDispatcher(handler);
//				if (dispatcher != null) {
//					try {
//						List<String> workers = dispatcher.getWorker(currentProcessTaskStepVo, config);
//						if (workers != null && workers.size() > 0) {
//							for (String worker : workers) {
//								if (StringUtils.isNotBlank(worker)) {
//									ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
//									workerVo.setUserId(worker);
//									workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//									workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
//									if (!workerList.contains(workerVo)) {
//										workerList.add(workerVo);
//									}
//								}
//							}
//						}
//					} catch (Exception ex) {
//						logger.error(ex.getMessage(), ex);
//					}
//				}
//			}
//		}
//		return workerList;
	}
}
