package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.framework.process.constvalue.IProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
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
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepAuditVo[].class, desc = "工单活动列表"),
		@Param(name = "Return[n].auditDetailList", explode = ProcessTaskStepAuditDetailVo[].class, desc = "工单活动详情列表")
	})
	@Description(desc = "工单活动列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessStepHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(processTaskStepId != null) {
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo == null) {
				throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
			}
			if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
				throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
			}
		}

		List<ProcessTaskStepAuditVo> resutlList = new ArrayList<>();
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
			for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
				JSONObject paramObj = new JSONObject();
				if(processTaskStepAudit.getProcessTaskStepId() != null) {
					//判断当前用户是否有权限查看该节点信息
					List<String> verifyActionList = new ArrayList<>();
					verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
					List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskStepAudit.getProcessTaskId(), processTaskStepAudit.getProcessTaskStepId(), verifyActionList);
					if(!actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
						continue;
					}
				}
				paramObj.put("processTaskStepName", processTaskStepAudit.getProcessTaskStepName());
				List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
				processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
				Iterator<ProcessTaskStepAuditDetailVo> iterator = processTaskStepAuditDetailList.iterator();
				while(iterator.hasNext()) {
					ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo = iterator.next();
					if(ProcessTaskAuditDetailType.TASKSTEP.getValue().equals(processTaskStepAuditDetailVo.getType())) {
						ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepAuditDetailVo.getNewContent());
						if(processTaskContentVo != null) {
							processTaskStepAudit.setNextStepId(Long.parseLong(processTaskContentVo.getContent()));
						}
					}
					IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
					if(auditDetailHandler != null) {
						auditDetailHandler.handle(processTaskStepAuditDetailVo);
						paramObj.putAll(processTaskStepAuditDetailVo.getParamObj());
					}
					if(ProcessTaskAuditDetailType.TASKSTEP.getValue().equals(processTaskStepAuditDetailVo.getType())) {
						processTaskStepAudit.setNextStepName(processTaskStepAuditDetailVo.getNewContent());
						iterator.remove();
					}
				}
				processTaskStepAudit.setDescription(FreemarkerUtil.transform(paramObj, IProcessTaskAuditType.getDescription(processTaskStepAudit.getAction())));
				resutlList.add(processTaskStepAudit);
			}
		}		
		return resutlList;
	}

}
