package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.service.ProcessService;

@Service
@AuthAction(name = "PROCESS_MODIFY")
public class ProcessSaveApi extends ApiComponentBase {

	@Autowired
	private ProcessService processService;

	@Override
	public String getToken() {
		return "process/save";
	}

	@Override
	public String getName() {
		return "流程保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid，为空表示创建流程", isRequired = false),
			@Param(name = "name", type = ApiParamType.STRING, desc = "流程名称", isRequired = true, length = 30, xss = true),
			@Param(name = "type", type = ApiParamType.INTEGER, desc = "流程类型", isRequired = true),
			@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1", isRequired = true),
			@Param(name = "config", type = ApiParamType.STRING, desc = "流程配置内容", isRequired = true),
			@Param(name = "belong", type = ApiParamType.ENUM, desc = "流程归属", rule = "bug,itsm,request,task")})
	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid") })
	@Description(desc = "流程保存接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.toJavaObject(jsonObj, ProcessVo.class);
		processVo.makeupFromConfigObj();
		processService.saveProcess(processVo);
		return processVo.getUuid();
	}

}
