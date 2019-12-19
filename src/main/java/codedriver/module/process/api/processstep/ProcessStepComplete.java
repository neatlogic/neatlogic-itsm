package codedriver.module.process.api.processstep;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;

import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessStepComplete extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processStep/complete";
	}

	@Override
	public String getName() {
		return "流程步骤完成接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid"),
			@Param(name = "channelUuid", type = ApiParamType.STRING, desc = "通道uuid"),
			@Param(name = "title", type = ApiParamType.STRING, desc = "标题"),
			@Param(name = "step", type = ApiParamType.JSONOBJECT, desc = "步骤信息")
			})
	@Output({})
	@Description(desc = "流程步骤完成接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		long processTaskStepId = jsonObj.getLongValue("processTaskStepId");
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepBaseInfoById(processTaskStepId);
		if (processTaskStepVo != null) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
			if (handler != null) {
				//processTaskStepVo.setParamObj(paramObj);
				handler.complete(processTaskStepVo);
			}
		} else {
			throw new RuntimeException("流程步骤不存在");

		}
		return null;
	}
	

}

