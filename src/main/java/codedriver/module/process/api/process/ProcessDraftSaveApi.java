package codedriver.module.process.api.process;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessDraftVo;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessDraftSaveApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;

	@Override
	public String getToken() {
		return "process/draft/save";
	}

	@Override
	public String getName() {
		return "流程草稿保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid", isRequired = true),
			@Param(name = "name", type = ApiParamType.STRING, xss = true, isRequired = false, maxLength = 50, desc = "流程名称"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "流程配置内容", isRequired = true) })
	@Output({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "草稿uuid") })
	@Description(desc = "流程草稿保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessDraftVo processDraftVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessDraftVo>() {
		});
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
		processMapper.insertProcessDraft(processDraftVo);
		return processDraftVo.getUuid();
	}
}
