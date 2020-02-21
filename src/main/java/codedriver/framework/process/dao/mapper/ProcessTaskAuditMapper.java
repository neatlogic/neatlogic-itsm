package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskStepAuditVo;

public interface ProcessTaskAuditMapper {
	
	public List<ProcessTaskStepAuditVo> getProcessTaskAuditList(ProcessTaskStepAuditVo processTaskStepAuditVo);
	
	public int insertProcessTaskStepAudit(ProcessTaskStepAuditVo processTaskStepAuditVo);
}
