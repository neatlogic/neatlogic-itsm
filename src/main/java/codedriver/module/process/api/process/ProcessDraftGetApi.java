package codedriver.module.process.api.process;

import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.PROCESS_MODIFY;

import codedriver.module.process.dao.mapper.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessDraftVo;
import codedriver.framework.process.exception.process.ProcessDraftNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDraftGetApi extends PrivateApiComponentBase {

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
		processDraftVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processDraftVo.getConfig()));
		return processDraftVo;
	}

}
