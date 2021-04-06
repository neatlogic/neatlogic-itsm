package codedriver.module.process.api.process;

import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_MODIFY;

import codedriver.module.process.dependency.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessDraftVo;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessReferencedCannotBeDeleteException;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;

	@Autowired
	private ScoreTemplateMapper scoreTemplateMapper;
	
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
		List<String> slaUuidList = processMapper.getSlaUuidListByProcessUuid(uuid);
		List<String> processStepUuidList = processMapper.getProcessStepUuidListByProcessUuid(uuid);
		DependencyManager.delete(NotifyPolicyProcessDependencyHandler.class, uuid);
		DependencyManager.delete(NotifyPolicyProcessStepDependencyHandler.class, processStepUuidList);
		DependencyManager.delete(NotifyPolicyProcessSlaDependencyHandler.class, slaUuidList);
		DependencyManager.delete(IntegrationProcessDependencyHandler.class, uuid);
		DependencyManager.delete(IntegrationProcessStepDependencyHandler.class, processStepUuidList);
		processMapper.deleteProcessByUuid(uuid);
		processMapper.deleteProcessStepByProcessUuid(uuid);
		processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
		processMapper.deleteProcessStepRelByProcessUuid(uuid);
		processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setProcessUuid(uuid);
		processMapper.deleteProcessDraft(processDraftVo);
		processMapper.deleteProcessFormByProcessUuid(uuid);
		processMapper.deleteProcessSlaByProcessUuid(uuid);
		scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(uuid);
		return uuid;
	}

}
