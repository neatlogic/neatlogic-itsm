package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.file.dto.FileVo;

public class ProcessTaskStepCommentVo {
	//private Long processTaskId;
	//private Long processTaskStepId;
	private Long auditId;
	private String content;
	private List<FileVo> fileList;
	//private List<String> fileUuidList;
	
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
	
	public void addFile(FileVo fileVo) {
		if(fileList == null) {
			fileList = new ArrayList<>();
		}
		fileList.add(fileVo);
	}
//	public Long getProcessTaskId() {
//		return processTaskId;
//	}
//	public void setProcessTaskId(Long processTaskId) {
//		this.processTaskId = processTaskId;
//	}
//	public Long getProcessTaskStepId() {
//		return processTaskStepId;
//	}
//	public void setProcessTaskStepId(Long processTaskStepId) {
//		this.processTaskStepId = processTaskStepId;
//	}
	public Long getAuditId() {
		return auditId;
	}
	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}
//	public List<String> getFileUuidList() {
//		return fileUuidList;
//	}
//	public void setFileUuidList(List<String> fileUuidList) {
//		this.fileUuidList = fileUuidList;
//	}
}
