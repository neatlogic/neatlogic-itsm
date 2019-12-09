package codedriver.module.process.dto;

public class ProcessStepHandlerVo implements Comparable<ProcessStepHandlerVo> {
	private String type;
	private String name;
	private String moduleId;
	private Integer isActive;
	private String icon;
	private Integer sort;

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	@Override
	public int compareTo(ProcessStepHandlerVo o) {
		return o.getSort() - this.getSort();
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
