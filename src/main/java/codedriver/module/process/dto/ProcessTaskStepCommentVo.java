package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.file.dto.FileVo;

public class ProcessTaskStepCommentVo {
	private String content;
	private List<FileVo> fileList;
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
}
