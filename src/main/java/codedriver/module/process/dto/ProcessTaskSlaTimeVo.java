package codedriver.module.process.dto;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskSlaTimeVo {
	@EntityField(name = "slaId", type = ApiParamType.LONG)
	private Long slaId;
	@EntityField(name = "超时日期（根据工作日历计算）", type = ApiParamType.STRING)
	private String expireTime;
	@EntityField(name = "超时日期（不考虑工作日历）", type = ApiParamType.STRING)
	private String realExpireTime;
	@EntityField(name = "总耗时，单位：毫秒", type = ApiParamType.LONG)
	private Long timeSum;
	@EntityField(name = "剩余时间（根据工作日历计算），单位：毫秒", type = ApiParamType.LONG)
	private Long timeLeft;
	@EntityField(name = "剩余时间（不考虑工作日历），单位：毫秒", type = ApiParamType.LONG)
	private Long realTimeLeft;

	public Long getSlaId() {
		return slaId;
	}

	public void setSlaId(Long slaId) {
		this.slaId = slaId;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getRealExpireTime() {
		return realExpireTime;
	}

	public void setRealExpireTime(String realExpireTime) {
		this.realExpireTime = realExpireTime;
	}

	public Long getTimeSum() {
		return timeSum;
	}

	public void setTimeSum(Long timeSum) {
		this.timeSum = timeSum;
	}

	public Long getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(Long timeLeft) {
		this.timeLeft = timeLeft;
	}

	public Long getRealTimeLeft() {
		return realTimeLeft;
	}

	public void setRealTimeLeft(Long realTimeLeft) {
		this.realTimeLeft = realTimeLeft;
	}
}
