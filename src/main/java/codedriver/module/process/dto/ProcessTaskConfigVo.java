package codedriver.module.process.dto;

public class ProcessTaskConfigVo {

	private String hash;
	private String config;

	public ProcessTaskConfigVo() {

	}

	public ProcessTaskConfigVo(String _hash, String _config) {
		hash = _hash;
		config = _config;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
