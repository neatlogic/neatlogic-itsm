package neatlogic.module.process.api.process;

import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ProcessDraftVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDraftClearApi extends PrivateApiComponentBase {

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
		@Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid，清空当前流程的草稿")
	})
	@Description(desc = "流程草稿清空接口，最后更新时间2020-02-18 15:01，修改参数说明")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setProcessUuid(jsonObj.getString("processUuid"));
		processDraftVo.setFcu(UserContext.get().getUserUuid(true));
		processMapper.deleteProcessDraft(processDraftVo);
		return null;
	}

}
