package codedriver.module.process.api.process;

import java.util.List;
import java.util.UUID;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessSlaVo;
import codedriver.framework.process.dto.ProcessStepRelVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.module.process.auth.label.PROCESS_MODIFY;
import codedriver.module.process.service.ProcessService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessCopyApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	@Autowired
	private ProcessService processService;
	
	@Override
	public String getToken() {
		return "process/copy";
	}

	@Override
	public String getName() {
		return "流程复制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被复制流程的uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "新流程名称")
	})
	@Output({
		@Param(name = "Return", explode = ProcessVo.class, desc = "新流程信息")
	})
	@Description(desc = "流程复制接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		ProcessVo processVo = processMapper.getProcessByUuid(uuid);
		if(processVo == null) {
			throw new ProcessNotFoundException(uuid);
		}
		processVo.setUuid(null);
		String name = jsonObj.getString("name");
		processVo.setName(name);
		if(processMapper.checkProcessNameIsRepeat(processVo) > 0) {
			throw new ProcessNameRepeatException(name);
		}
		
		String newUuid = processVo.getUuid();
		String config = processVo.getConfig();
		config = config.replace(uuid, newUuid);
		
		ProcessStepVo processStepVo = new ProcessStepVo();
		processStepVo.setProcessUuid(uuid);
		List<ProcessStepVo> processStepList = processMapper.searchProcessStep(processStepVo);
		for(ProcessStepVo processStep : processStepList) {
			String newStepUuid = UUID.randomUUID().toString().replace("-", "");
			config = config.replace(processStep.getUuid(), newStepUuid);
		}
		List<ProcessStepRelVo> processStepRelList = processMapper.getProcessStepRelByProcessUuid(uuid);
		for(ProcessStepRelVo processStepRel : processStepRelList) {
			String newRelUuid = UUID.randomUUID().toString().replace("-", "");
			config = config.replace(processStepRel.getUuid(), newRelUuid);
		}
		List<ProcessSlaVo> processSlaList = processMapper.getProcessSlaByProcessUuid(uuid);
		for(ProcessSlaVo processSla : processSlaList) {
			String newSlaUuid = UUID.randomUUID().toString().replace("-", "");
			config = config.replace(processSla.getUuid(), newSlaUuid);
		}
		processVo.setConfig(config);
		processVo.setConfigObj(null);
		processVo.makeupConfigObj();
		processService.saveProcess(processVo);
		processVo.setConfig(null);
		return processVo;
	}

}
