package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@AuthAction(name = "PROCESS_MODIFY")
@OperationType(type = OperationTypeEnum.UPDATE)
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
