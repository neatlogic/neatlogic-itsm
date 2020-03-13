package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskAuditListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/audit/list";
	}

	@Override
	public String getName() {
		return "工单活动列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepAuditVo[].class, desc = "工单活动列表")
	})
	@Description(desc = "工单活动列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.VIEW)) {
			return null;
		}
		
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isEmpty(processTaskStepAuditList)) {
			return null;
		}
		List<ProcessTaskStepAuditVo> resutlList = new ArrayList<>();
		for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
			if(ProcessTaskStepAction.SAVE.getValue().equals(processTaskStepAudit.getAction())) {
				continue;
			}
			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAudit.getAuditDetailList()) {
				IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
				if(auditDetailHandler != null) {
					auditDetailHandler.handle(processTaskStepAuditDetailVo);
				}
			}
			resutlList.add(processTaskStepAudit);
		}
		return resutlList;
	}

}
