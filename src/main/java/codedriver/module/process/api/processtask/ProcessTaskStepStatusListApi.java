package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.actionauthorityverificationhandler.core.IProcessTaskStepUserActionAuthorityVerificationHandler;
import codedriver.framework.process.actionauthorityverificationhandler.core.ProcessTaskStepUserActionAuthorityVerificationHandlerFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ProcessTaskStepVo;
@Service
public class ProcessTaskStepStatusListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/status/List";
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
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤状态列表")
	})
	@Description(desc = "工单全部步骤状态列表接口，用于流程图上显示步骤状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		IProcessTaskStepUserActionAuthorityVerificationHandler handler = ProcessTaskStepUserActionAuthorityVerificationHandlerFactory.getHandler(ProcessTaskStepAction.VIEW.getValue());
		if(handler != null) {
			if(!handler.test(processTaskId, processTaskStepId)) {
				return null;
			}
		}
		
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
		if(CollectionUtils.isEmpty(processTaskStepList)) {
			return null;
		}
		for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
			processTaskStepVo.setMajorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.MAJOR.getValue()));
			processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.MINOR.getValue()));
			processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.AGENT.getValue()));
			processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
		}
		return processTaskStepList;
	}

}
