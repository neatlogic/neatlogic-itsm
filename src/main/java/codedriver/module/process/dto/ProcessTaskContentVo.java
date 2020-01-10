package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import codedriver.framework.asynchronization.threadlocal.UserContext;

public class ProcessTaskContentVo {
	private String hash;
	private String content;

	public ProcessTaskContentVo() {

	}

	public ProcessTaskContentVo(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHash() {
		if (StringUtils.isBlank(hash) && StringUtils.isNotBlank(content)) {
			hash = DigestUtils.md5DigestAsHex(content.getBytes());
		}
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
