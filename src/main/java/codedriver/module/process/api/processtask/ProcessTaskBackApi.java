package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
@AuthAction(name = "PROCESS_MODIFY")
public class ProcessTaskBackApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/back";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "工单回退接口";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId",
					type = ApiParamType.LONG,
					desc = "工单Id",
					isRequired = true),
			@Param(name = "processTaskStepId",
			type = ApiParamType.LONG,
			desc = "工单步骤Id",
			isRequired = true)
	})
	@Output({})
	@Description(desc = "工单回退接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		Long processTaskStepId = jsonObj.getLong("processtaskStepId");
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if (processTaskStepVo != null) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
			if (handler != null) {
				processTaskStepVo.setParamObj(jsonObj);
				handler.back(processTaskStepVo);
			}
		} else {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		return result;
	}

}
