package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
@Service
public class SubtaskAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.SUBTASK.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getOldContent())) {
			ProcessTaskStepSubtaskVo oldProcessTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getOldContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});		
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(oldProcessTaskStepSubtaskVo.getContentHash());
			if(processTaskContentVo != null) {
				oldProcessTaskStepSubtaskVo.setContent(processTaskContentVo.getContent());
			}
			processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(oldProcessTaskStepSubtaskVo));
		}
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepSubtaskVo.getContentHash());
			if(processTaskContentVo != null) {
				processTaskStepSubtaskVo.setContent(processTaskContentVo.getContent());
			}
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(processTaskStepSubtaskVo));
		}
	}

}
