package codedriver.module.process.audithandler.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class FormAuditHandler implements IProcessTaskStepAuditDetailHandler {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FORM.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = JSON.parseArray(oldContent, ProcessTaskFormAttributeDataVo.class);
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(newContent, ProcessTaskFormAttributeDataVo.class);
		
	}

}
