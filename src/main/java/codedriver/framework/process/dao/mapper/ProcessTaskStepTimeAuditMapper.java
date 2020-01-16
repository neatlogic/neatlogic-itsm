package codedriver.framework.process.dao.mapper;

import codedriver.module.process.dto.ProcessTaskStepTimeAuditVo;

public interface ProcessTaskStepTimeAuditMapper {
	public ProcessTaskStepTimeAuditVo getLastProcessTaskStepTimeAuditByStepId(Long processTaskStepId);

	public int updateProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);

	public int insertProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);
}
