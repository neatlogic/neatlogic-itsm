package codedriver.framework.process.dto;

import java.io.Serializable;

public class ProcessStepRelVo implements Serializable {
	private static final long serialVersionUID = 1970685757497902601L;
	private String processUuid;
	private String uuid;
	private String fromStepUuid;
	private String toStepUuid;
	private String condition;

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof ProcessStepRelVo))
			return false;

		final ProcessStepRelVo rel = (ProcessStepRelVo) other;
		try {
			if (getUuid().equals(rel.getUuid())) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = getUuid().hashCode() * 29;
		return result;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFromStepUuid() {
		return fromStepUuid;
	}

	public void setFromStepUuid(String fromStepUuid) {
		this.fromStepUuid = fromStepUuid;
	}

	public String getToStepUuid() {
		return toStepUuid;
	}

	public void setToStepUuid(String toStepUuid) {
		this.toStepUuid = toStepUuid;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

}
