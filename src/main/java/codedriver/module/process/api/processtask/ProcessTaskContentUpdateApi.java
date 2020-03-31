package codedriver.module.process.api.processtask;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class ProcessTaskContentUpdateApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
	
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
			throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
		}

		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler == null) {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		handler.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.UPDATE);
		
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
				
		String newContentHash = null;
		String content = jsonObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			newContentHash = contentVo.getHash();
			//如果新的上报描述内容和原来的上报描述内容不一样，则生成活动
			if(!Objects.equals(newContentHash, oldContentHash)) {
				processTaskMapper.replaceProcessTaskContent(contentVo);
				processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, newContentHash));
				jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), newContentHash);
			}else {
				return null;
			}
			
		}else if(oldContentHash != null){
			processTaskMapper.deleteProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, oldContentHash));
		}
		//生成活动	
		jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
		processTaskStepVo.setParamObj(jsonObj);
		handler.activityAudit(processTaskStepVo, ProcessTaskStepAction.UPDATECONTENT);
		return null;
	}

}
