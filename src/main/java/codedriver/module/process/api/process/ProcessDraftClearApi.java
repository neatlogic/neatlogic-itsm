package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessDraftVo;
@Service
@Transactional
public class ProcessDraftClearApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/draft/clear";
	}

	@Override
	public String getName() {
		return "流程草稿清空接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid", isRequired = true)
	})
	@Description(desc = "流程草稿清空接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setFcu(UserContext.get().getUserId());
		processDraftVo.setProcessUuid(jsonObj.getString("processUuid"));
		processMapper.deleteProcessDraft(processDraftVo);
		return null;
	}

}
