package codedriver.framework.process.dto;

import java.io.Serializable;

import codedriver.framework.common.dto.BasePageVo;

public class DataCubeFieldVo extends BasePageVo implements Serializable {
	/** 
	* @Fields serialVersionUID : TODO 
	*/
	private static final long serialVersionUID = -6569412276972262351L;
	private String name;
	private String dataCubeUuid;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataCubeUuid() {
		return dataCubeUuid;
	}

	public void setDataCubeUuid(String dataCubeUuid) {
		this.dataCubeUuid = dataCubeUuid;
	}

}
