package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;

public class ProcessTaskStepCommentVo {
	private Long auditId;
	private String content;
	private List<FileVo> fileList;
	
	public ProcessTaskStepCommentVo(ProcessTaskStepAuditVo processTaskStepAuditVo) {
		auditId = processTaskStepAuditVo.getId();
		List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailListt = processTaskStepAuditVo.getAuditDetailList();
		for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailListt) {
			IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
			if(auditDetailHandler != null) {
				auditDetailHandler.handle(processTaskStepAuditDetailVo);
			}
			if(ProcessTaskAuditDetailType.CONTENT.getValue().equals(processTaskStepAuditDetailVo.getType())) {
				content = processTaskStepAuditDetailVo.getNewContent();
			}else if(ProcessTaskAuditDetailType.FILE.getValue().equals(processTaskStepAuditDetailVo.getType())){
				FileVo fileVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<FileVo>() {});
				if(fileList == null) {
					fileList = new ArrayList<>();
				}
				fileList.add(fileVo);
			}
		}
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<FileVo> getFileList() {
		return fileList;
	}
	public void setFileList(List<FileVo> fileList) {
		this.fileList = fileList;
	}

	public Long getAuditId() {
		return auditId;
	}
	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

}
