package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.service.ProcessService;

@Service
@Transactional
public class ProcessCopyApi extends ApiComponentBase {

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
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, length = 50, desc = "新流程名称")
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
		processVo.makeupFromConfigObj();
		processService.saveProcess(processVo);
		processVo.setConfig(null);
		return processVo;
	}

}
