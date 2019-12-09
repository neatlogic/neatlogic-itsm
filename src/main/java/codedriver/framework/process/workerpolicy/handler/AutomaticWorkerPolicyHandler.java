package codedriver.framework.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.workerdispatcher.IWorkerDispatcher;
import codedriver.framework.process.workerdispatcher.WorkerDispatcherFactory;
import codedriver.module.process.constvalue.WorkerPolicy;

@Service
public class AutomaticWorkerPolicyHandler implements IWorkerPolicyHandler {
	Logger logger = LoggerFactory.getLogger(AutomaticWorkerPolicyHandler.class);

	@Override
	public String getType() {
		return WorkerPolicy.AUTOMATIC.getValue();
	}

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
		if (workerPolicyVo.getConfigObj() != null) {
			String handler = workerPolicyVo.getConfigObj().getString("handler");
			JSONObject config = workerPolicyVo.getConfigObj().getJSONObject("config");
			if (StringUtils.isNotBlank(handler)) {
				IWorkerDispatcher dispatcher = WorkerDispatcherFactory.getDispatcher(handler);
				if (dispatcher != null) {
					try {
						List<String> workers = dispatcher.getWorker(currentProcessTaskStepVo, config);
						if (workers != null && workers.size() > 0) {
							for (String worker : workers) {
								if (StringUtils.isNotBlank(worker)) {
									ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
									workerVo.setUserId(worker);
									workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
									workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
									if (!workerList.contains(workerVo)) {
										workerList.add(workerVo);
									}
								}
							}
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			}
		}
		return workerList;
	}
}
