package codedriver.module.process.api.process;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessStepHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "process/step/handler/list";
	}

	@Override
	public String getName() {
		return "流程组件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({@Param(name = "Retrun", explode = ProcessStepHandlerVo[].class, desc = "流程组件列表")})
	@Description(desc = "流程组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return ProcessStepHandlerFactory.getActiveProcessStepHandler();
	}

}
