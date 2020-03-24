package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
public class ProcessTaskContentUpdateApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/content/update";
	}

	@Override
	public String getName() {
		return "工单上报描述内容更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "content", type = ApiParamType.STRING, xss = true, desc = "描述")
	})
	@Description(desc = "工单上报描述内容更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.UPDATE)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.UPDATE.getText());
		}
		
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
		//获取上报描述内容hash
		String oldContentHash = null;
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
		if(!processTaskStepContentList.isEmpty()) {
			oldContentHash = processTaskStepContentList.get(0).getContentHash();
		}
		
		String content = jsonObj.getString("content");
		if(oldContentHash == null && StringUtils.isBlank(content)) {
			return null;
		}
		
		String newContentHash = null;
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			newContentHash = contentVo.getHash();
			//如果新的上报描述内容和原来的上报描述内容不一样，则生成活动
			if(!newContentHash.equals(oldContentHash)) {
				processTaskMapper.replaceProcessTaskContent(contentVo);
				processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, newContentHash));
			}else {
				return null;
			}
			
		}else if(oldContentHash != null){
			processTaskMapper.deleteProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, oldContentHash));
		}
		//生成活动
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo(processTaskId, processTaskStepId, UserContext.get().getUserId(true), ProcessTaskStepAction.UPDATECONTENT.getValue());
		processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);		
		processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.CONTENT.getValue(), oldContentHash, newContentHash));	
		
		return null;
	}

}
