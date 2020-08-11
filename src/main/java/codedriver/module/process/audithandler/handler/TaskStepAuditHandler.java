package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
@Service
public class TaskStepAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.TASKSTEP.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			Long processTaskStepId = Long.parseLong(newContent);
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo != null) {
				processTaskStepAuditDetailVo.getParamObj().put("nextStepId", processTaskStepId);
				processTaskStepAuditDetailVo.setNewContent(processTaskStepVo.getName());
				processTaskStepAuditDetailVo.getParamObj().put("nextStepName", processTaskStepVo.getName());
			}
		}
		return 0;
	}

}
