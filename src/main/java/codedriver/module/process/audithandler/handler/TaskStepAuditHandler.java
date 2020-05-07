package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
@Service
public class TaskStepAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.TASKSTEP.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			Long processTaskStepId = Long.parseLong(newContent);
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo != null) {
				processTaskStepAuditDetailVo.setNewContent(processTaskStepVo.getName());
			}
		}		
	}

}
