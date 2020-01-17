package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.ProcessTaskStepTimeAuditVo;

public interface ProcessTaskStepTimeAuditMapper {
	public List<ProcessTaskStepTimeAuditVo> getProcessTaskStepTimeAuditBySlaId(Long slaId);

	public ProcessTaskStepTimeAuditVo getLastProcessTaskStepTimeAuditByStepId(Long processTaskStepId);

	public int updateProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);

	public int insertProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);
}
