package codedriver.module.process.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;

public class WorktimeVo extends BasePageVo {

	@EntityField(name = "工作时间窗口uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "工作时间窗口名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "是否激活", type = ApiParamType.INTEGER)
	private Integer isActive;
	@EntityField(name = "最后一次修改用户", type = ApiParamType.STRING)
	private String lcu;
	@EntityField(name = "最后一次修改时间", type = ApiParamType.LONG)
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
	@EntityField(name = "每周工作时段的定义", type = ApiParamType.STRING)
	private String config;
	
	@EntityField(name = "年份列表", type = ApiParamType.JSONARRAY)
	private List<Integer> yearList;
	
	@EntityField(name = "工作时段列表", type = ApiParamType.JSONARRAY)
	private Set<String> workingHoursSet;
	
	private transient String keyword;
	
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
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public List<Integer> getYearList() {
		return yearList;
	}
	public void setYearList(List<Integer> yearList) {
		this.yearList = yearList;
	}
	public Set<String> getWorkingHoursSet() {
		return workingHoursSet;
	}
	public void setWorkingHoursSet(Set<String> workingHoursSet) {
		this.workingHoursSet = workingHoursSet;
	}

}
