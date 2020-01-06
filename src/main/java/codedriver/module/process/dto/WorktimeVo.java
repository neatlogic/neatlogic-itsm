package codedriver.module.process.dto;

import java.util.Date;
import java.util.UUID;

public class WorktimeVo {

	private String uuid;
	private String name;
	private Integer isActive;
	private String lcu;
	private Date lcd;
	/**
	 * {
	 * 		"monday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"tuesday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"wednesday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"thursday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"friday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"saturday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}],
	 *		"sunday":[{"startTime":"9:00","endTime":"12:00"},{"startTime":"14:00","endTime":"18:00"}]
	 * }
	 */
	private String config;
	
	public synchronized String getUuid() {
		if(uuid == null) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	public String getLcu() {
		return lcu;
	}
	public void setLcu(String lcu) {
		this.lcu = lcu;
	}
	public Date getLcd() {
		return lcd;
	}
	public void setLcd(Date lcd) {
		this.lcd = lcd;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}

}
