package codedriver.module.process.api.processtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepStatusListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/status/list";
	}

	@Override
	public String getName() {
		return "工单全部步骤状态列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤状态列表")
	})
	@Description(desc = "工单全部步骤状态列表接口，用于流程图上显示步骤状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");	
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
	        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
	        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
	        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
	        }
	        for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
				if(CollectionUtils.isNotEmpty(majorUserList)) {
					processTaskStepVo.setMajorUser(majorUserList.get(0));
				}
				processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
				processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.AGENT.getValue()));
				processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
				processTaskStepVo.setConfig(processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash()));
				ProcessStepHandlerVo processStepHandlerConfig = handlerConfigMap.get(processTaskStepVo.getHandler());
				if(processStepHandlerConfig != null) {
					processTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
				}
			}
		}	
		return processTaskStepList;
	}

}
