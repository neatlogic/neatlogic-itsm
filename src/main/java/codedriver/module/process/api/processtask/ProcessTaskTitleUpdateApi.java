package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
@Service
@Transactional
public class ProcessTaskTitleUpdateApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/title/update";
	}

	@Override
	public String getName() {
		return "工单标题更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "title", type = ApiParamType.REGEX, rule="^[A-Za-z_\\d\\u4e00-\\u9fa5]+$",isRequired = true, desc = "标题")
		//@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "标题")
	})
	@Description(desc = "工单标题更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		//获取步骤信息
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
			throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'工单：'" + processTaskId + "'的步骤");
		}
		String oldTile = processTaskVo.getTitle();
		String title = jsonObj.getString("title");
		//如果标题跟原来的标题一样，不生成活动
		if(title.equals(oldTile)) {
			return null;
		}
		//更新标题
		processTaskVo.setTitle(title);
		processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
		//生成活动
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.UPDATETITLE.getValue());
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepAuditVo.setUserId(UserContext.get().getUserId(true));
		processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
		
		processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.TITLE.getValue(), oldTile, title));
		return null;
	}
}
