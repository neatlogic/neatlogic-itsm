package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.actionauthorityverificationhandler.core.IProcessTaskStepUserActionAuthorityVerificationHandler;
import codedriver.framework.process.actionauthorityverificationhandler.core.ProcessTaskStepUserActionAuthorityVerificationHandlerFactory;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepCommentVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
@Service
public class ProcessTaskStepListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/list";
	}

	@Override
	public String getName() {
		return "工单步骤列表接口";
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
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "工单步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		IProcessTaskStepUserActionAuthorityVerificationHandler handler = ProcessTaskStepUserActionAuthorityVerificationHandlerFactory.getHandler(ProcessTaskStepAction.VIEW.getValue());
		if(handler != null) {
			if(!handler.test(processTaskId, processTaskStepId)) {
				return null;
			}
		}
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		//开始步骤
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		ProcessTaskStepVo startStepVo = processTaskStepList.get(0);
		startStepVo.setMajorUserList(processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), UserType.MAJOR.getValue()));
		startStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), UserType.AGENT.getValue()));
		//上报描述内容和附件
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(startStepVo.getId());
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.STARTPROCESS.getValue());
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
			ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo();
			List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailListt = processTaskStepAuditList.get(0).getAuditDetailList();
			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailListt) {
				IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
				if(auditDetailHandler != null) {
					auditDetailHandler.handle(processTaskStepAuditDetailVo);
				}
				if(ProcessTaskAuditDetailType.CONTENT.getValue().equals(processTaskStepAuditDetailVo.getType())) {
					comment.setContent(processTaskStepAuditDetailVo.getNewContent());
				}else if(ProcessTaskAuditDetailType.FILE.getValue().equals(processTaskStepAuditDetailVo.getType())){
					FileVo fileVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<FileVo>() {});
					comment.addFile(fileVo);
				}
			}
			startStepVo.setComment(comment);
		}
		resultList.add(startStepVo);
		//其他处理步骤
		processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				if(processTaskStepVo.getIsActive().intValue() == 0) {
					continue;
				}
				processTaskStepVo.setMajorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.MAJOR.getValue()));
				processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.MINOR.getValue()));
				processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), UserType.AGENT.getValue()));
				//步骤评论列表
				processTaskStepAuditVo = new ProcessTaskStepAuditVo();
				processTaskStepAuditVo.setProcessTaskId(processTaskId);
				processTaskStepAuditVo.setProcessTaskStepId(processTaskStepVo.getId());
				processTaskStepAuditVo.setAction(ProcessTaskStepAction.COMMENT.getValue());
				processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
				if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
					List<ProcessTaskStepCommentVo> commentList = new ArrayList<>();
					for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
						List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailListt = processTaskStepAudit.getAuditDetailList();
						ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo();
						for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailListt) {
							IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
							if(auditDetailHandler != null) {
								auditDetailHandler.handle(processTaskStepAuditDetailVo);
							}
							if(ProcessTaskAuditDetailType.CONTENT.getValue().equals(processTaskStepAuditDetailVo.getType())) {
								comment.setContent(processTaskStepAuditDetailVo.getNewContent());
							}else if(ProcessTaskAuditDetailType.FILE.getValue().equals(processTaskStepAuditDetailVo.getType())){
								FileVo fileVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<FileVo>() {});
								comment.addFile(fileVo);
							}
						}
						commentList.add(comment);
					}
					processTaskStepVo.setCommentList(commentList);
				}
				resultList.add(processTaskStepVo);
			}
		}
		//按开始时间正序排序
		resultList.sort((step1, step2) -> (int)(step1.getStartTime().getTime() - step2.getStartTime().getTime()));
		return resultList;
	}

}
