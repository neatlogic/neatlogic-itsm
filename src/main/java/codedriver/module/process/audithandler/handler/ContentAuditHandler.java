package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class ContentAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.CONTENT.getValue();
	}

	@Override
	protected void myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(oldContent);
			if(processTaskContentVo != null) {
				processTaskStepAuditDetailVo.setOldContent(processTaskContentVo.getContent());
			}
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(newContent);
			if(processTaskContentVo != null) {
				processTaskStepAuditDetailVo.setNewContent(processTaskContentVo.getContent());
			}
		}
		
	}

}
