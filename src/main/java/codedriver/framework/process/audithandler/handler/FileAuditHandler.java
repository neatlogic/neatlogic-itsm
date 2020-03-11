package codedriver.framework.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class FileAuditHandler implements IProcessTaskStepAuditDetailHandler{
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FILE.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			FileVo fileVo = fileMapper.getFileByUuid(oldContent);
			if(fileVo != null) {
				processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(fileVo));
			}
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			FileVo fileVo = fileMapper.getFileByUuid(newContent);
			if(fileVo != null) {
				processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(fileVo));
			}
		}
	}

}
