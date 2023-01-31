package neatlogic.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerdispatcher.core.IWorkerDispatcher;
import neatlogic.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;

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
	public int isOnlyOnceExecute() {
		return 0;
	}

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (MapUtils.isEmpty(workerPolicyVo.getConfigObj())) {
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
		if(MapUtils.isEmpty(handlerConfig)) {
			return processTaskStepWorkerList;
		}
		List<String> workerList = dispatcher.getWorker(currentProcessTaskStepVo, handlerConfig);
		if(CollectionUtils.isEmpty(workerList)) {
			return processTaskStepWorkerList;
		}
		for(String userUuid : workerList) {
			if (StringUtils.isBlank(userUuid)) {
				continue;
			}
			ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), userUuid, ProcessUserType.MAJOR.getValue());
			if (!processTaskStepWorkerList.contains(workerVo)) {
				processTaskStepWorkerList.add(workerVo);
			}
		}
		return processTaskStepWorkerList;
	}
}
