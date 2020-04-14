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
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskAuditListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepAuditVo[].class, desc = "工单活动列表"),
		@Param(name = "Return[n].auditDetailList", explode = ProcessTaskStepAuditDetailVo[].class, desc = "工单活动详情列表")
	})
	@Description(desc = "工单活动列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		List<String> verifyActionList = new ArrayList<>();
		verifyActionList.add(ProcessTaskStepAction.POCESSTASKVIEW.getValue());
		verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
		List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, null, verifyActionList);
		if(!actionList.removeAll(verifyActionList)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.POCESSTASKVIEW.getText());
		}
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
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
			//判断当前用户是否有权限查看该节点信息
			verifyActionList = new ArrayList<>();
			verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
			actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskStepAudit.getProcessTaskId(), processTaskStepAudit.getProcessTaskStepId(), verifyActionList);
			if(!actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
				continue;
			}
			List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
			processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailList) {
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
