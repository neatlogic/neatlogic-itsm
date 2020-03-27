package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
public class ProcessTaskCommentApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "processtask/comment";
	}

	@Override
	public String getName() {
		return "工单回复接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "描述"),
		@Param(name = "fileUuidList", type=ApiParamType.JSONARRAY, desc = "附件uuid列表")
	})
	@Description(desc = "工单回复接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.COMMENT)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.COMMENT.getText());
		}
		
		//删除暂存活动
		ProcessTaskStepAuditVo auditVo = new ProcessTaskStepAuditVo();
		auditVo.setProcessTaskId(processTaskId);
		auditVo.setProcessTaskStepId(processTaskStepId);
		auditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
		auditVo.setUserId(UserContext.get().getUserId(true));
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(auditVo);
		for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
			processTaskMapper.deleteProcessTaskStepAuditById(processTaskStepAudit.getId());
		}

		String content = jsonObj.getString("content");
		ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
		processTaskMapper.replaceProcessTaskContent(contentVo);
		jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
				
		String fileUuidListStr = jsonObj.getString("fileUuidList");
		if(StringUtils.isNotBlank(fileUuidListStr)) {
			List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
			if(CollectionUtils.isNotEmpty(fileUuidList)) {
				for(String fileUuid : fileUuidList) {
					if(fileMapper.getFileByUuid(fileUuid) == null) {
						throw new ProcessTaskRuntimeException("上传附件uuid:'" + fileUuid + "'不存在");
					}
				}
			}
		}
		//生成活动	
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			currentProcessTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.COMMENT);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		return null;
	}

}
