package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.process.ProcessDraftNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessDraftVo;
@Service
@IsActived
public class ProcessDraftGetApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/draft/get";
	}

	@Override
	public String getName() {
		return "流程草稿信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "草稿uuid")})
	@Output({@Param(explode = ProcessDraftVo.class)})
	@Description(desc="流程草稿信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		ProcessDraftVo processDraftVo = processMapper.getProcessDraftByUuid(uuid);
		if(processDraftVo == null) {
			throw new ProcessDraftNotFoundException(uuid);
		}
		return processDraftVo;
	}

}
