package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.asynchronization.threadlocal.UserContext;

public class ProcessTaskContentVo {
	private Long id;
	private String content;
	private String editor;

	public ProcessTaskContentVo() {

	}

	public ProcessTaskContentVo(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEditor() {
		if (StringUtils.isBlank(editor)) {
			editor = UserContext.get().getUserId();
		}
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

}
