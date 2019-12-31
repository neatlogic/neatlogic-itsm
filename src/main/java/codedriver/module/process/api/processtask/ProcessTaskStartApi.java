package codedriver.module.process.api.processtask;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.exception.ProcessTaskException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskConfigVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.service.ProcessService;

@Service
public class ProcessTaskStartApi extends ApiComponentBase {

	@Autowired
	private ProcessService processService;

	@Override
	public String getToken() {
		return "processTask/start";
	}

	@Override
	public String getName() {
		return "工单上报接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "processUuid",
					type = ApiParamType.STRING,
					isRequired = true,
					desc = "流程uuid"),
			@Param(name = "channelUuid",
					type = ApiParamType.STRING,
					isRequired = true,
					desc = "通道uuid"),
			@Param(name = "title",
					type = ApiParamType.STRING,
					isRequired = true,
					desc = "标题"),
			@Param(name = "step",
					type = ApiParamType.JSONOBJECT,
					isRequired = true,
					desc = "步骤信息") })
	@Output({
			@Param(name = "processTaskId",
					type = ApiParamType.LONG,
					desc = "工单id") })
	@Description(desc = "工单上报接口")
	@Example(example = "{\r\n" + "	\"processUuid\":\"90aabb319a534399be7f5f86765849d3\",\r\n" + "	\"channelUuid\":\"tongdao111\",\r\n" + "	\"title\":\"test5\",\r\n" + "	\"step\": \r\n" + "			{\r\n" + "				\"content\": \"<p>哈哈哈哈哈哈</p>\"\r\n" + "			}\r\n" + "}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String processUuid = jsonObj.getString("processUuid");
		ProcessTaskVo processTaskVo = new ProcessTaskVo();
		ProcessVo processVo = processService.getProcessByUuid(processUuid);
		// 根据uuid去查process的config 然后生成md5
		// 判断是否在process_history中存在
		if (processVo != null) {
		} else {
			throw new ProcessTaskException("请输入正确的流程编号!");
		}

		String currentUserId = UserContext.get().getUserId();
		jsonObj.put("owner", currentUserId);// 工单上报人
		ProcessStepVo startStepVo = processService.getProcessStartStep(processUuid);
		if (startStepVo != null) {
			ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo(startStepVo);
			startTaskStep.setParamObj(jsonObj);
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startStepVo.getHandler());
			if (handler != null) {
				handler.startProcess(startTaskStep);
			}
		}
		// return "上报成功!";
		return processTaskVo.getId();
	}

}
