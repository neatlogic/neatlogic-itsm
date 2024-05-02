package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.dto.ProcessDraftVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDraftSaveApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;

	@Override
	public String getToken() {
		return "process/draft/save";
	}

	@Override
	public String getName() {
		return "nmpap.processdraftsaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "processUuid", type = ApiParamType.STRING, desc = "term.itsm.processuuid", isRequired = true),
			@Param(name = "name", type = ApiParamType.STRING, xss = true, isRequired = false, maxLength = 50, desc = "common.name"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config", isRequired = true)
	})
	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "草稿uuid")
	})
	@Description(desc = "nmpap.processdraftsaveapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessDraftVo processDraftVo = JSONObject.toJavaObject(jsonObj, ProcessDraftVo.class);
		processDraftVo.setFcu(UserContext.get().getUserUuid(true));
		if (processMapper.checkProcessDraftIsExists(processDraftVo) > 0) {
			return null;
		}
		String earliestUuid = null;
		if (processMapper.checkProcessIsExists(processDraftVo.getProcessUuid()) == 0) {
			ProcessDraftVo processDraft = new ProcessDraftVo();
			processDraft.setFcu(UserContext.get().getUserUuid(true));
			earliestUuid = processMapper.getEarliestProcessDraft(processDraft);
		} else {
			earliestUuid = processMapper.getEarliestProcessDraft(processDraftVo);
		}

		if (earliestUuid != null) {
			processMapper.deleteProcessDraftByUuid(earliestUuid);
		}
//		processDraftVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processDraftVo.getConfig()));
		processMapper.insertProcessDraft(processDraftVo);
		return processDraftVo.getUuid();
	}
}
