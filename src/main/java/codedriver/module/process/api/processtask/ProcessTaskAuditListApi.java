package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskAuditTypeFactory;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessOperateManager;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskAuditListApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private SelectContentByHashMapper selectContentByHashMapper;
	
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
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        try {
//            ProcessStepUtilHandlerFactory.getHandler().verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.POCESSTASKVIEW, true);
            new ProcessOperateManager.Builder(processTaskMapper, userMapper)
            .addProcessTaskId(processTaskVo.getId())
            .addOperationType(ProcessTaskOperationType.POCESSTASKVIEW)
            .addCheckOperationType(processTaskVo.getId(), ProcessTaskOperationType.POCESSTASKVIEW)
            .withIsThrowException(true)
            .build()
            .check();
        }catch(ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
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
				    ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepAudit.getProcessTaskStepId());
				    if(processTaskStepVo == null) {
				        throw new ProcessTaskStepNotFoundException(processTaskStepAudit.getProcessTaskStepId().toString());
				    }
				    IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
                    if(processStepUtilHandler == null) {
                        throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
                    }
//                    if(!processStepUtilHandler.verifyOperationAuthoriy(processTaskStepAudit.getProcessTaskId(), processTaskStepAudit.getProcessTaskStepId(), ProcessTaskOperationType.VIEW, false)) {
//                        continue;
//                    }
                    if(!new ProcessOperateManager.Builder(processTaskMapper, userMapper)
                        .addProcessTaskStepId(processTaskStepAudit.getProcessTaskId(), processTaskStepAudit.getProcessTaskStepId())
                        .addOperationType(ProcessTaskOperationType.VIEW)
                        .addCheckOperationType(processTaskStepAudit.getProcessTaskStepId(), ProcessTaskOperationType.VIEW)
                        .build()
                        .check()) {
                        continue;
                    }
				}
				paramObj.put("processTaskStepName", processTaskStepAudit.getProcessTaskStepName());
				if(processTaskStepAudit.getStepStatusVo() != null) {
					paramObj.put("stepStatusVo", processTaskStepAudit.getStepStatusVo());
				}
				List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
				processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
				Iterator<ProcessTaskStepAuditDetailVo> iterator = processTaskStepAuditDetailList.iterator();
				while(iterator.hasNext()) {
					ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo = iterator.next();
					if(ProcessTaskAuditDetailType.TASKSTEP.getValue().equals(processTaskStepAuditDetailVo.getType())) {
						String content = selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepAuditDetailVo.getNewContent());
						if(StringUtils.isNotBlank(content)) {
							processTaskStepAudit.setNextStepId(Long.parseLong(content));
						}
					}
					IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
					if(auditDetailHandler != null) {
						int isShow = auditDetailHandler.handle(processTaskStepAuditDetailVo);
						paramObj.putAll(processTaskStepAuditDetailVo.getParamObj());
						if(isShow == 0) {
							iterator.remove();
						}
					}
					if(ProcessTaskAuditDetailType.TASKSTEP.getValue().equals(processTaskStepAuditDetailVo.getType())) {
						processTaskStepAudit.setNextStepName(processTaskStepAuditDetailVo.getNewContent());						
					}
				}
				processTaskStepAudit.setDescription(FreemarkerUtil.transform(paramObj, ProcessTaskAuditTypeFactory.getDescription(processTaskStepAudit.getAction())));
				resutlList.add(processTaskStepAudit);
			}
		}		
		return resutlList;
	}

}
