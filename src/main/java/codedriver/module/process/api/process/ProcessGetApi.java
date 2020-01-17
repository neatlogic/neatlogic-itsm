package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessVo;

@Service
public class ProcessGetApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/get";
	}

	@Override
	public String getName() {
		return "获取单个流程图数据接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")})
	@Output({@Param(explode = ProcessVo.class)})
	@Description(desc="获取单个流程图数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		ProcessVo processVo = processMapper.getProcessByUuid(uuid);
		if(processVo == null) {
			throw new ProcessNotFoundException(uuid);
		}
		return processVo;
	}

}
