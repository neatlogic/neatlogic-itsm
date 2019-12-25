package codedriver.module.process.dto;

public class ProcessConfigHistoryVo {

	private String processMd;
	private String historyProcessConfig;
	public ProcessConfigHistoryVo(String processMd, String historyProcessConfig) {
		this.processMd = processMd;
		this.historyProcessConfig = historyProcessConfig;
	}

	public String getProcessMd() {
		return processMd;
	}

	public void setProcessMd(String processMd) {
		this.processMd = processMd;
	}

	public String getHistoryProcessConfig() {
		return historyProcessConfig;
	}
	public void setHistoryProcessConfig(String historyProcessConfig) {
		this.historyProcessConfig = historyProcessConfig;
	}
	
}
 