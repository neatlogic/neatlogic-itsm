package codedriver.module.process.dto;

public class ChannelPriorityVo {

	private String channelUuid;
	
	private String priorityUuid;
	
	private Integer isDefault;

	public String getChannelUuid() {
		return channelUuid;
	}

	public void setChannelUuid(String channelUuid) {
		this.channelUuid = channelUuid;
	}

	public String getPriorityUuid() {
		return priorityUuid;
	}

	public void setPriorityUuid(String priorityUuid) {
		this.priorityUuid = priorityUuid;
	}

	public Integer getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Integer isDefault) {
		this.isDefault = isDefault;
	}
}
