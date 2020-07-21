package codedriver.module.process.api.process;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessService;

@Service
@Transactional
@AuthAction(name = "PROCESS_MODIFY")
@OperationType(type = OperationTypeEnum.CREATE)
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
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid", isRequired = true),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "流程名称"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "流程配置内容", isRequired = true)
		})
	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid") 
			})
	@Description(desc = "流程保存接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.toJavaObject(jsonObj, ProcessVo.class);
		processVo.makeupConfigObj();
		processService.saveProcess(processVo);
		return processVo.getUuid();
	}

}
