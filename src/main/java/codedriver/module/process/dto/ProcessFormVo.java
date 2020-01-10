package codedriver.module.process.dto;

import java.io.Serializable;

import codedriver.framework.common.dto.BasePageVo;

public class ProcessFormVo extends BasePageVo implements Serializable {
	/** 
	* @Fields serialVersionUID : TODO 
	*/
	private static final long serialVersionUID = 3563625084673916939L;
	private String processUuid;
	private String formUuid;

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}
}
