package codedriver.module.process.dto;

public class WorkerDispatcherVo {
	private String name;
	private String handler;
	private Integer isActive;
	private String help;
	private String configPage;
	private String moduleId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getConfigPage() {
		return configPage;
	}

	public void setConfigPage(String configPage) {
		this.configPage = configPage;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
