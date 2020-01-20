package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessReferencedCannotBeDeleteException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class ProcessDeleteApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/delete";
	}

	@Override
	public String getName() {
		return "流程删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid")
	})
	@Description(desc = "流程删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(processMapper.checkProcessIsExists(uuid) == 0) {
			throw new ProcessNotFoundException(uuid);
		}
		if(processMapper.getProcessReferenceCount(uuid) > 0) {
			throw new ProcessReferencedCannotBeDeleteException(uuid);
		}
		
		processMapper.deleteProcessByUuid(uuid);
		processMapper.deleteProcessStepByProcessUuid(uuid);
		processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
		processMapper.deleteProcessStepRelByProcessUuid(uuid);
		processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
		processMapper.deleteProcessStepTimeoutPolicyByProcessUuid(uuid);
		return uuid;
	}

}